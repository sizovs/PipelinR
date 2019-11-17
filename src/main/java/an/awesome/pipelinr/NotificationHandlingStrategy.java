package an.awesome.pipelinr;

import java.util.List;

public interface NotificationHandlingStrategy {

  void handle(List<Runnable> runnableNotifications);

}
