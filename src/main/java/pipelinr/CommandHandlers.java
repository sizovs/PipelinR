package pipelinr;

import one.util.streamex.StreamEx;

import java.util.List;

import static java.util.Arrays.asList;

public class CommandHandlers {

    private final List<Command.Handler> commandHandlers;

    public CommandHandlers(Command.Handler... commandHandlers) {
        this.commandHandlers = asList(commandHandlers);
    }

    public StreamEx<Command.Handler> stream() {
        return StreamEx.of(commandHandlers);
    }
}
