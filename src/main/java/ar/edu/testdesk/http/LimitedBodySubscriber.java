package ar.edu.testdesk.http;

import java.io.ByteArrayOutputStream;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.Flow;

final class LimitedBodySubscriber implements HttpResponse.BodySubscriber<byte[]> {
  private final CompletableFuture<byte[]> result = new CompletableFuture<>();
  private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
  private Flow.Subscription subscription;

  public CompletionStage<byte[]> getBody() {
    return result;
  }

  public void onSubscribe(Flow.Subscription s) {
    subscription = s;
    s.request(1);
  }

  public void onNext(List<ByteBuffer> chunks) {
    for (ByteBuffer chunk : chunks) {
      if (buffer.size() + chunk.remaining() > 1_048_576) {
        subscription.cancel();
        result.completeExceptionally(new IllegalStateException("Respuesta mayor a 1 MiB"));
        return;
      }
      byte[] bytes = new byte[chunk.remaining()];
      chunk.get(bytes);
      buffer.writeBytes(bytes);
    }
    subscription.request(1);
  }

  public void onError(Throwable error) {
    result.completeExceptionally(error);
  }

  public void onComplete() {
    result.complete(buffer.toByteArray());
  }
}
