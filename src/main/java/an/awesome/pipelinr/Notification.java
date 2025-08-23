package an.awesome.pipelinr;

import com.google.common.reflect.TypeToken;
import java.util.stream.Stream;

public interface Notification {

  default void send(Pipeline pipeline) {
    pipeline.send(this);
  }

  interface Handler<N extends Notification> {
    void handle(N notification);

    default boolean matches(N notification) {
      TypeToken<N> notificationType = new TypeToken<N>(getClass()) {};
      return notificationType.getRawType().isAssignableFrom(notification.getClass());

    }
  }

  @FunctionalInterface
  interface Middlewares {
    Stream<Middleware> supply();
  }

  @FunctionalInterface
  interface Middleware {
    <N extends Notification> void invoke(N notification, Middleware.Next next);

    interface Next {
      void invoke();
    }
  }
}
