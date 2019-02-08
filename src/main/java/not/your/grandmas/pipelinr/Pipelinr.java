package not.your.grandmas.pipelinr;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;

public class Pipelinr implements Pipeline {

    private final Router router;
    private final PipelineSteps steps;

    public Pipelinr(CommandHandlers commandHandlers, PipelineSteps steps) {
        this.router = new ToFirstMatching(commandHandlers);
        this.steps = checkNotNull(steps, "Steps must not be null");
    }

    public <R, C extends Command<R>> R send(C command) {
        checkNotNull(command, "Command must not be null");

        PipelineStep.Next<R> handleCommand = new Handle<>(command);

        return steps
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

    private class ToFirstMatching implements Router {

        private final CommandHandlers commandHandlers;

        public ToFirstMatching(CommandHandlers commandHandlers) {
            this.commandHandlers = checkNotNull(commandHandlers, "Command handlers must not be null");
        }

        @Override
        @SuppressWarnings("unchecked")
        public <C extends Command<R>, R> Command.Handler<C, R> route(C command) {
            List<Command.Handler> matchingHandlers = commandHandlers
                    .stream()
                    .filter(handler -> handler.matches(command))
                    .collect(toList());

            boolean noMatches = matchingHandlers.isEmpty();
            if (noMatches) {
                throw new CommandHandlerNotFoundException(command);
            }

            boolean moreThanOneMatchingHandlerFound = matchingHandlers.size() > 1;
            if (moreThanOneMatchingHandlerFound) {
                throw new CommandHasMultipleHandlersException(command, matchingHandlers);
            }

            return matchingHandlers.get(0);
        }

    }

    private interface Router {

        <C extends Command<R>, R> Command.Handler<C, R> route(C command);
    }

}
