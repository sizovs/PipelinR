package an.awesome.pipelinr;

import java.util.stream.Stream;

@FunctionalInterface
public interface CommandHandlers {

  Stream<Command.Handler> supply();
}
