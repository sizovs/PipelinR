package not.your.grandmas.pipelinr;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

public class CommandHandlers {

    private final Supplier supplierOfCommandHandlers;

    public CommandHandlers(Supplier supplier) {
        this.supplierOfCommandHandlers = supplier;
    }

    public CommandHandlers(Collection<Command.Handler> commandHandlers) {
        this.supplierOfCommandHandlers = commandHandlers::stream;
    }

    public CommandHandlers(Command.Handler... commandHandlers) {
        this.supplierOfCommandHandlers = () -> Arrays.stream(commandHandlers);
    }

    protected Stream<Command.Handler> stream() {
        return supplierOfCommandHandlers.supply();
    }

    @FunctionalInterface
    public interface Supplier {
        Stream<Command.Handler> supply();
    }

}
