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
  void supportsAbstractCommandHandlers() {
    // given
    HandlerThatExtendsAbstractClass handlerThatExtendsAbstractClass =
        new HandlerThatExtendsAbstractClass();

    // and
    Pipelinr pipelinr = new Pipelinr().with(() -> Stream.of(handlerThatExtendsAbstractClass));

    // when
    pipelinr.send(new Ping("hi"));

    // then:
    assertThat(handlerThatExtendsAbstractClass.receivedPings).containsOnly(new Ping("hi"));
  }

  interface GenericInterfaceFoo<A> {
  }

  interface GenericInterfaceBar<B> {
  }

  @Test
  void supportsHandlersThatImplementGenericInterfaces() {
    class SubjectHandler implements GenericInterfaceBar<Integer>, Command.Handler<Ping, Voidy>, GenericInterfaceFoo<Integer> {

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
    Hi hi = new Hi();
    PingSaver pingSaver = new PingSaver();

    // and
    Pipelinr pipelinr = new Pipelinr().with(() -> Stream.of(hi, pingSaver));

    // when
    pipelinr.send(new Ping("hi"));

    // and
    pipelinr.send(new Ping("bye"));

    // then:
    assertThat(hi.receivedPings).containsOnly(new Ping("hi"));
    assertThat(pingSaver.receivedPings).containsOnly(new Ping("bye"));
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
    public boolean equals(Object obj) {
      if (obj instanceof Ping) {
        Ping other = (Ping) obj;
        return other.message.equals(this.message);
      }
      return false;
    }

    static class PingSaver implements Handler<Ping, Voidy> {

      Collection<Ping> receivedPings = new ArrayList<>();

      @Override
      public Voidy handle(Ping command) {
        this.receivedPings.add(command);
        return new Voidy();
      }

      @Override
      public boolean matches(Ping command) {
        return command.message.equals("bye");
      }
    }

    static class Hi implements Handler<Ping, Voidy> {

      Collection<Ping> receivedPings = new ArrayList<>();

      @Override
      public Voidy handle(Ping command) {
        this.receivedPings.add(command);
        return new Voidy();
      }

      @Override
      public boolean matches(Ping command) {
        return command.message.equals("hi");
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

    static class HandlerThatExtendsAbstractClass extends AbstractHandler<Ping, Voidy> {

      Collection<Ping> receivedPings = new ArrayList<>();

      @Override
      public Voidy handle(Ping command) {
        this.receivedPings.add(command);
        return new Voidy();
      }
    }

    abstract static class AbstractHandler<C extends Command<R>, R> implements Handler<C, R> {}
  }
}
