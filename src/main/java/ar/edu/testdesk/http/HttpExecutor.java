package ar.edu.testdesk.http;

import ar.edu.testdesk.model.*;
import ar.edu.testdesk.service.Validation;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.*;
import java.time.*;
import java.util.concurrent.*;
import java.util.regex.*;

public final class HttpExecutor implements AutoCloseable {
  private final HttpClient client =
      HttpClient.newBuilder()
          .connectTimeout(Duration.ofSeconds(5))
          .followRedirects(HttpClient.Redirect.NEVER)
          .build();

  public TestResult execute(TestCase t) {
    Validation.testCase(t);
    if (!t.active()) throw new IllegalArgumentException("No se puede ejecutar un caso inactivo.");
    long start = System.nanoTime();
    LocalDateTime date = LocalDateTime.now(ZoneOffset.UTC);
    Redactor redactor = new Redactor(t);
    Integer actual = null;
    String state = "ERROR", message, excerpt = "";
    CompletableFuture<HttpResponse<byte[]>> request = null;
    try {
      HttpRequest.Builder builder =
          HttpRequest.newBuilder(URI.create(t.url())).timeout(Duration.ofSeconds(12));
      Validation.headers(t.headers()).forEach(builder::header);
      builder.method(
          t.method(),
          t.method().equals("GET") || t.body().isEmpty()
              ? HttpRequest.BodyPublishers.noBody()
              : HttpRequest.BodyPublishers.ofString(t.body(), StandardCharsets.UTF_8));
      request = client.sendAsync(builder.build(), info -> new LimitedBodySubscriber());
      HttpResponse<byte[]> response = request.get(12, TimeUnit.SECONDS);
      actual = response.statusCode();
      String body =
          new String(
              response.body(), charset(response.headers().firstValue("Content-Type").orElse("")));
      boolean codeMatches = actual == t.expectedStatus();
      boolean textMatches = t.expectedText().isEmpty() || body.contains(t.expectedText());
      state = codeMatches && textMatches ? "PASS" : "FAIL";
      message =
          state.equals("PASS")
              ? "Todas las aserciones coinciden."
              : (codeMatches ? "" : "Código HTTP distinto del esperado. ")
                  + (textMatches ? "" : "No se encontró el texto esperado.");
      String safe = redactor.clean(body);
      excerpt = safe.substring(0, Math.min(4000, safe.length()));
    } catch (TimeoutException e) {
      message = "Tiempo de espera agotado (12 segundos).";
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      message = "Ejecución interrumpida.";
    } catch (ExecutionException | IllegalArgumentException e) {
      Throwable cause = e instanceof ExecutionException ? e.getCause() : e;
      message =
          "Error técnico: "
              + cause.getClass().getSimpleName()
              + ". Revise conexión, certificado, headers y límite de respuesta (1 MiB).";
    } finally {
      if (request != null && !request.isDone()) request.cancel(true);
    }
    return new TestResult(
        null,
        0,
        t.id(),
        Redactor.limit(redactor.clean(t.title()), 160),
        t.method(),
        redactor.url(t.url()),
        t.expectedStatus(),
        actual,
        redactor.clean(t.expectedText()),
        date,
        TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start),
        state,
        message,
        excerpt);
  }

  private Charset charset(String contentType) {
    Matcher m = Pattern.compile("(?i)charset\\s*=\\s*[\"']?([^;\\s\"']+)").matcher(contentType);
    if (m.find()) {
      try {
        return Charset.forName(m.group(1));
      } catch (IllegalArgumentException e) {
        return StandardCharsets.UTF_8;
      }
    }
    return StandardCharsets.UTF_8;
  }

  @Override
  public void close() {
    client.shutdownNow();
  }
}
