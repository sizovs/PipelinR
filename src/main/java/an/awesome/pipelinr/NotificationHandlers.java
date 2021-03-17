package an.awesome.pipelinr;

import java.util.stream.Stream;

@FunctionalInterface
public interface NotificationHandlers {

  Stream<Notification.Handler> supply();
}
