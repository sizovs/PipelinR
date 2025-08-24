package an.awesome.pipelinr;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class CommandHandlingTest {

  @Test
  void supportsCommandsWithGenerics() {
    class Cmd<G> implements Command<G> {}
    class Handler implements Command.Handler<Cmd<String>, String> {
      @SuppressWarnings({"rawtypes"})
      @Override
      public String handle(Cmd command) {
        return "HANDLED";
      }
    }

    Pipeline pipeline = new Pipelinr().with(() -> Stream.of(new Handler()));

    String results = pipeline.send(new Cmd<>());
    assertThat(results).isEqualTo("HANDLED");
  }

  @Test
  void handlesCommandsThatAreSubtypesOfAGenericArgument() {
    // given
    PingHandler pingHandler = new PingHandler();
    SomethingElseHandler somethingElseHandler = new SomethingElseHandler();
    Pipeline pipeline = new Pipelinr().with(() -> Stream.of(pingHandler, somethingElseHandler));

    // and
    Ping ping = new Ping();
    PingOnSteroids pingOnSteroids = new PingOnSteroids();

    SomethingElse somethingElse = new SomethingElse();

    // when
    pipeline.send(ping);
    pipeline.send(pingOnSteroids);
    pipeline.send(somethingElse);

    // then
    assertThat(pingHandler.commands).containsOnly(ping, pingOnSteroids);
    assertThat(somethingElseHandler.commands).containsOnly(somethingElse);
  }

  static class Ping implements Command<Voidy> {}

  static class PingOnSteroids extends Ping {}

  static class PingHandler implements Command.Handler<Ping, Voidy> {
    private final Collection<Ping> commands = new ArrayList<>();

    @Override
    public Voidy handle(Ping command) {
      commands.add(command);
      return new Voidy();
    }
  }

  static class SomethingElse implements Command<Voidy> {}

  static class SomethingElseHandler implements Command.Handler<SomethingElse, Voidy> {
    private final Collection<SomethingElse> commands = new ArrayList<>();

    @Override
    public Voidy handle(SomethingElse command) {
      commands.add(command);
      return new Voidy();
    }
  }
}
