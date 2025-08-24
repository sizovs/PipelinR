package an.awesome.pipelinr;

import java.util.stream.Stream;

public interface Command<R> {

  default R execute(Pipeline pipeline) {
    return pipeline.send(this);
  }

  interface Handler<C extends Command<R>, R> {

    R handle(C command);

    default boolean matches(C command) {
      Generic<C> commandType = new Generic<C>(getClass()) {};
      return commandType.resolve().isAssignableFrom(command.getClass());
    }
  }

  @FunctionalInterface
  interface Middlewares {
    Stream<Middleware> supply();
  }

  @FunctionalInterface
  interface Middleware {
    <R, C extends Command<R>> R invoke(C command, Next<R> next);

    interface Next<T> {
      T invoke();
    }
  }

  interface Router {
    <C extends Command<R>, R> Handler<C, R> route(C command);
  }
}
