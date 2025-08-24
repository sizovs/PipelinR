package an.awesome.pipelinr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import an.awesome.pipelinr.PipelinrTest.Ping.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class PipelinrTest {

  @Test
  void supportsAbstractHandlers() {
    abstract class AbstractHandler<C extends Command<R>, R> implements Command.Handler<C, R> {}

    class SubjectHandler extends AbstractHandler<Ping, Voidy> {
      final Collection<Ping> pings = new ArrayList<>();

      @Override
      public Voidy handle(Ping command) {
        this.pings.add(command);
        return new Voidy();
      }
    }

    // given
    SubjectHandler subjectHandler = new SubjectHandler();

    // and
    Pipelinr pipelinr = new Pipelinr().with(() -> Stream.of(subjectHandler));

    // when
    pipelinr.send(new Ping("hi"));

    // then:
    assertThat(subjectHandler.pings).containsOnly(new Ping("hi"));
  }

  interface GenericInterfaceFoo<A> {}

  interface GenericInterfaceBar<B> {}

  @Test
  void supportsHandlersThatImplementGenericInterfaces() {
    class SubjectHandler
        implements GenericInterfaceBar<Integer>,
            Command.Handler<Ping, Voidy>,
            GenericInterfaceFoo<Integer> {

      final Collection<Ping> pings = new ArrayList<>();

      @Override
      public Voidy handle(Ping command) {
        this.pings.add(command);
        return new Voidy();
      }
    }

    // given
    SubjectHandler handler = new SubjectHandler();

    // and
    Pipelinr pipelinr = new Pipelinr().with(() -> Stream.of(handler));

    // when
    pipelinr.send(new Ping("hi"));

    // then:
    assertThat(handler.pings).containsOnly(new Ping("hi"));
  }

  @Test
  void executesCommandMiddlewaresInOrder() {
    // given
    List<String> invokedMiddlewareIds = new ArrayList<>();

    // and
    class IdentifiableMiddleware implements Command.Middleware {
      private final String id;

      private IdentifiableMiddleware(String id) {
        this.id = id;
      }

      @Override
      public <R, C extends Command<R>> R invoke(C command, Next<R> next) {
        invokedMiddlewareIds.add(id);
        return next.invoke();
      }
    }

    // and
    Command.Middleware foo = new IdentifiableMiddleware("foo");

    // and
    Command.Middleware bar = new IdentifiableMiddleware("bar");

    // and
    Command.Middleware baz = new IdentifiableMiddleware("baz");

    // and
    Pipelinr pipelinr =
        new Pipelinr().with(() -> Stream.of(new Pong2())).with(() -> Stream.of(foo, bar, baz));

    // when
    new Ping().execute(pipelinr);

    // then
    assertThat(invokedMiddlewareIds).containsExactly("foo", "bar", "baz");
  }

  @Test
  void supportsCustomHandlerMatching() {
    // given
    Bye bye = new Bye();
    Goodnight goodnight = new Goodnight();

    // and
    Pipelinr pipelinr = new Pipelinr().with(() -> Stream.of(goodnight, bye));

    // when
    pipelinr.send(new Ping("goodnight"));

    // and
    pipelinr.send(new Ping("bye"));

    // then:
    assertThat(goodnight.pings).containsOnly(new Ping("goodnight"));
    assertThat(bye.pings).containsOnly(new Ping("bye"));
  }

  @Test
  void throwsIfSentCommandHasNoMatchingHandler() {
    // given
    Pipelinr pipelinr = new Pipelinr();

    // when
    Throwable e =
        assertThrows(
            CommandHandlerNotFoundException.class,
            () -> {
              pipelinr.send(new Ping());
            });

    // then
    assertThat(e).hasMessage("Cannot find a matching handler for Ping command");
  }

  @Test
  void throwsIfSentCommandHasMultipleHandlers() {
    // given
    Pipelinr pipelinr = new Pipelinr().with(() -> Stream.of(new Pong1(), new Pong2()));

    // when
    Throwable e =
        assertThrows(
            CommandHasMultipleHandlersException.class,
            () -> {
              pipelinr.send(new Ping());
            });

    // then
    assertThat(e)
        .hasMessage("Command Ping must have a single matching handler, but found 2 (Pong1, Pong2)");
  }

  static class Ping implements Command<Voidy> {

    private final String message;

    Ping() {
      this("");
    }

    Ping(String message) {
      this.message = message;
    }

    @Override
    public boolean equals(Object other) {
      if (other instanceof Ping) {
        Ping that = (Ping) other;
        return that.message.equals(this.message);
      }
      return false;
    }

    static class Bye implements Handler<Ping, Voidy> {

      Collection<Ping> pings = new ArrayList<>();

      @Override
      public Voidy handle(Ping command) {
        this.pings.add(command);
        return new Voidy();
      }

      @Override
      public boolean matches(Ping command) {
        return command.message.equals("bye");
      }
    }

    static class Goodnight implements Handler<Ping, Voidy> {

      Collection<Ping> pings = new ArrayList<>();

      @Override
      public Voidy handle(Ping command) {
        this.pings.add(command);
        return new Voidy();
      }

      @Override
      public boolean matches(Ping command) {
        return command.message.equals("goodnight");
      }
    }

    static class Pong1 implements Handler<Ping, Voidy> {
      @Override
      public Voidy handle(Ping command) {
        System.out.println("Pong 1");
        return new Voidy();
      }
    }

    static class Pong2 implements Handler<Ping, Voidy> {
      @Override
      public Voidy handle(Ping command) {
        System.out.println("Pong 2");
        return new Voidy();
      }
    }
  }
}
