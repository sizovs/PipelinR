package an.awesome.pipelinr;

import java.util.stream.Stream;

public interface Notification {
  
  default void send(Pipeline pipeline) {
    pipeline.send(this);
  }

  interface Handler<N extends Notification> {
    void handle(N notification);

    default boolean matches(N notification) {
      Class handlerType = getClass();
      Class commandType = notification.getClass();
      return new FirstGenericArgOf(handlerType).isAssignableFrom(commandType);
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
