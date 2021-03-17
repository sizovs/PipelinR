package an.awesome.pipelinr;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

/**
 * Run all notification handlers asynchronously.
 *
 * <p>Returns when all handlers are finished.
 *
 * <p>In case of any exception(s), they will be captured in an AggregateException.
 */
public class Async implements NotificationHandlingStrategy {

  private final ExecutorService threadPool;

  public Async(ExecutorService threadPool) {
    this.threadPool = threadPool;
  }

  @Override
  public void handle(List<Runnable> runnableNotifications) {
    Collection<Throwable> exceptions = new CopyOnWriteArrayList<>();
    CompletableFuture.runAsync(
            () -> {
              runnableNotifications.forEach(
                  it -> {
                    try {
                      it.run();
                    } catch (Exception e) {
                      exceptions.add(e);
                    }
                  });
            },
            threadPool)
        .join();

    if (!exceptions.isEmpty()) {
      throw new AggregateException(exceptions);
    }
  }
}
