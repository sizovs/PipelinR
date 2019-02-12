package an.awesome.pipelinr;

import java.util.List;
import java.util.stream.Stream;

import static an.awesome.pipelinr.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;

public class Pipelinr implements Pipeline {

    private final Command.Router router;
    private final StreamSupplier<PipelineStep> steps;

    public Pipelinr(StreamSupplier<Command.Handler> commandHandlers) {
        this(commandHandlers, Stream::empty);
    }

    public Pipelinr(StreamSupplier<Command.Handler> commandHandlers, StreamSupplier<PipelineStep> steps) {
        this.router = new ToFirstMatchAmong(commandHandlers);
        this.steps = checkArgument(steps, "Steps must not be null");
    }

    public <R, C extends Command<R>> R send(C command) {
        checkArgument(command, "Command must not be null");

        PipelineStep.Next<R> handleCommand = new Handle<>(command);

        return steps
                .supplyEx()
                .foldRight(handleCommand, (step, next) -> () -> step.invoke(command, next))
                .invoke();
    }

    private class Handle<R, C extends Command<R>> implements PipelineStep.Next<R> {

        private final C command;

        public Handle(C command) {
            this.command = command;
        }

        @Override
        public R invoke() {
            Command.Handler<C, R> handler = router.route(command);
            return handler.handle(command);
        }
    }

    private class ToFirstMatchAmong implements Command.Router {

        private final StreamSupplier<Command.Handler> commandHandlers;

        public ToFirstMatchAmong(StreamSupplier<Command.Handler> commandHandlers) {
            this.commandHandlers = checkArgument(commandHandlers, "Command handlers must not be null");
        }

        @Override
        @SuppressWarnings("unchecked")
        public <C extends Command<R>, R> Command.Handler<C, R> route(C command) {
            List<Command.Handler> matchingHandlers = commandHandlers
                    .supply()
                    .filter(handler -> handler.matches(command))
                    .collect(toList());

            boolean noMatches = matchingHandlers.isEmpty();
            if (noMatches) {
                throw new CommandHandlerNotFoundException(command);
            }

            boolean moreThanOneMatch = matchingHandlers.size() > 1;
            if (moreThanOneMatch) {
                throw new CommandHasMultipleHandlersException(command, matchingHandlers);
            }

            return matchingHandlers.get(0);
        }

    }

}
