package pipelinr;

import java.util.Collection;

import static java.util.stream.Collectors.joining;

public class CommandHasMultipleHandlersException extends RuntimeException {

    private final String message;

    public CommandHasMultipleHandlersException(Command command, Collection<Command.Handler> matchingHandlers) {
        String commandName = command.getClass().getSimpleName();
        String handlerNames = matchingHandlers
                .stream()
                .map(it -> it.getClass().getSimpleName())
                .collect(joining(", "));

        this.message = "Command " + commandName +
                " must have a single matching handler, but found " + matchingHandlers.size() + " (" + handlerNames + ")";
    }


    @Override
    public String getMessage() {
        return message;
    }
}
