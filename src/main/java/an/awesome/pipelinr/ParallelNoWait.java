package an.awesome.pipelinr;

import static java.util.concurrent.CompletableFuture.runAsync;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Run each notification handler in a thread pool.
 *
 * <p>Returns immediately and does not wait for any handlers to finish.
 *
 * <p>Note that you cannot capture any exceptions.
 */
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
