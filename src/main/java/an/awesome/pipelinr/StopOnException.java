package an.awesome.pipelinr;

import java.util.List;

/**
 * Run each notification handler after one another.
 *
 * <p>Returns when all handlers are finished or an exception has been thrown.
 *
 * <p>In case of an exception, any handlers after that will not be run.
 */
public class StopOnException implements NotificationHandlingStrategy {

  @Override
  public void handle(List<Runnable> runnableNotifications) {
    runnableNotifications.forEach(Runnable::run);
  }
}
