package not.your.grandmas.pipelinr;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public class CommandHandlers {

    private final List<Command.Handler> commandHandlers;

    public CommandHandlers(Command.Handler... commandHandlers) {
        this.commandHandlers = asList(commandHandlers);
    }

    public Stream<Command.Handler> stream() {
        return commandHandlers.stream();
    }
}
