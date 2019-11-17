package an.awesome.pipelinr;

import java.util.List;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.CompletableFuture.runAsync;

/**
 * Run each notification handler in a thread pool.
 *
 * Returns immediately and does not wait for any handlers to finish.
 *
 * Note that you cannot capture any exceptions.
**/
 public class ParallelNoWait implements NotificationHandlingStrategy {

  private final ExecutorService executorService;

  public ParallelNoWait(ExecutorService executorService) {
    this.executorService = executorService;
  }

  @Override
  public void handle(List<Runnable> runnableNotifications) {
    runnableNotifications.forEach(it -> runAsync(it, executorService));
  }
}
