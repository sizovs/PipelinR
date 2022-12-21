package an.awesome.pipelinr;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class CommandHandlingTest {

  @Test
  void supportsCommandsWithGenerics() {
    class CommandWithGenerics<G> implements Command<G> {}

    class HandlerForCommandWithGenerics
        implements Command.Handler<CommandWithGenerics<String>, String> {
      @SuppressWarnings({"rawtypes"})
      @Override
      public String handle(CommandWithGenerics command) {
        return "HANDLED";
      }
    }

    Pipeline pipeline = new Pipelinr().with(() -> Stream.of(new HandlerForCommandWithGenerics()));

    String results = pipeline.send(new CommandWithGenerics<>());
    assertThat(results).isEqualTo("HANDLED");
  }

  @Test
  void handlesCommandsThatAreSubtypesOfAGenericArgument() {
    // given
    Ping.Handler pingHandler = new Ping.Handler();
    NotAPing.Handler notAPingHandler = new NotAPing.Handler();
    Pipeline pipeline = new Pipelinr().with(() -> Stream.of(pingHandler, notAPingHandler));

    // and
    Ping ping = new Ping();
    SmartPing smartPing = new SmartPing();
    NotAPing notAPing = new NotAPing();

    // when
    pipeline.send(ping);
    pipeline.send(smartPing);
    pipeline.send(notAPing);

    // then
    assertThat(pingHandler.handled).containsOnly(ping, smartPing);
    assertThat(notAPingHandler.handled).containsOnly(notAPing);
  }

  static class Ping implements Command<Voidy> {

    static class Handler implements Command.Handler<Ping, Voidy> {

      private Collection<Command> handled = new ArrayList<>();

      @Override
      public Voidy handle(Ping command) {
        handled.add(command);
        return new Voidy();
      }
    }
  }

  static class SmartPing extends Ping {}

  static class NotAPing implements Command<Voidy> {
    static class Handler implements Command.Handler<NotAPing, Voidy> {

      private Collection<Command> handled = new ArrayList<>();

      @Override
      public Voidy handle(NotAPing command) {
        handled.add(command);
        return new Voidy();
      }
    }
  }
}
