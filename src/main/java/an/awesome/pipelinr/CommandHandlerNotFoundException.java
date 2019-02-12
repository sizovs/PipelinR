package an.awesome.pipelinr;

public class CommandHandlerNotFoundException extends RuntimeException {

    private final String commandClass;

    public CommandHandlerNotFoundException(Command command) {
        this.commandClass = command.getClass().getSimpleName();
    }

    @Override
    public String getMessage() {
        return "Cannot find a matching handler for " + commandClass + " command";
    }
}
