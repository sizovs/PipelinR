package an.awesome.pipelinr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Run each notification handler in a thread pool.
 *
 * Returns when all threads (handlers) are finished.
 *
 * In case of any exception(s), they are captured in an AggregateException.
 */
public class ParallelWhenAll implements NotificationHandlingStrategy {

  private final ExecutorService threadPool;

  public ParallelWhenAll(ExecutorService threadPool) {
    this.threadPool = threadPool;
  }

  @Override
  public void handle(List<Runnable> runnableNotifications) {
    Collection<Throwable> exceptions = new ArrayList<>();
    List<CompletableFuture<Void>> futures = runnableNotifications
            .stream()
            .map(runnable -> CompletableFuture.runAsync(runnable, threadPool).exceptionally(throwable -> {
              exceptions.add(throwable);
              return null;
            }))
            .collect(Collectors.toList());
    CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[]{})
    ).join();
    if (!exceptions.isEmpty()) {
      throw new AggregateException(exceptions);
    }
  }
}
