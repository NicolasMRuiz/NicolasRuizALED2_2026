package ar.edu.testdesk.http;

import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public final class DemoServer implements AutoCloseable {
  public static final int PORT = 8089;
  private HttpServer server;
  private ExecutorService executor;

  public synchronized boolean isRunning() {
    return server != null;
  }

  public synchronized void start() {
    if (server != null) return;
    try {
      HttpServer candidate =
          HttpServer.create(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), PORT), 0);
      executor = Executors.newVirtualThreadPerTaskExecutor();
      candidate.setExecutor(executor);
      candidate.createContext("/", this::handle);
      candidate.start();
      server = candidate;
    } catch (IOException e) {
      throw new IllegalStateException(
          "No se pudo iniciar la API demo en 127.0.0.1:"
              + PORT
              + ". Cierre el programa que ocupa ese puerto y vuelva a intentar.",
          e);
    }
  }

  private void handle(HttpExchange exchange) {
    try (exchange) {
      String path = exchange.getRequestURI().getPath();
      int status = 200;
      String body;
      switch (path) {
        case "/ok" -> body = "TestDesk: respuesta correcta";
        case "/error" -> {
          status = 500;
          body = "error demo";
        }
        case "/echo" -> {
          if (!exchange.getRequestMethod().equals("POST")) {
            status = 405;
            body = "Use POST";
          } else {
            byte[] bytes = exchange.getRequestBody().readNBytes(65537);
            if (bytes.length > 65536) {
              status = 413;
              body = "Body demasiado grande";
            } else body = new String(bytes, StandardCharsets.UTF_8);
          }
        }
        case "/slow" -> {
          try {
            Thread.sleep(15000);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
          }
          body = "Respuesta demorada";
        }
        default -> {
          status = 404;
          body = "no encontrado";
        }
      }
      byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
      exchange.sendResponseHeaders(status, bytes.length);
      exchange.getResponseBody().write(bytes);
    } catch (IOException e) {
      // Es normal que /slow pierda el cliente cuando vence su timeout.
      System.err.println("API demo: conexión finalizada (" + e.getClass().getSimpleName() + ").");
    }
  }

  @Override
  public synchronized void close() {
    if (server != null) {
      server.stop(0);
      server = null;
    }
    if (executor != null) {
      executor.shutdownNow();
      executor = null;
    }
  }
}
