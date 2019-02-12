package an.awesome.pipelinr;


import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;

public class RoutingPipeline implements Pipeline {

    private final Collection<Route> routes;

    public RoutingPipeline(Route... routes) {
        this.routes = Arrays.asList(routes);
    }

    @Override
    public <R, C extends Command<R>> R send(C command) {
        Pipeline pipeline = routes
                .stream()
                .filter(pipelineRoute -> pipelineRoute.condition().test(command))
                .map(Route::pipeline)
                .findFirst()
                .orElseThrow(() -> new RouteNotFoundException(command));

        return pipeline.send(command);
    }

    public static class Route {

        private final Pipeline pipeline;
        private final Predicate<Command> condition;

        public Route(Predicate<Command> condition, Pipeline pipeline) {
            this.pipeline = pipeline;
            this.condition = condition;
        }

        public Predicate<Command> condition() {
            return condition;
        }

        public Pipeline pipeline() {
            return pipeline;
        }
    }

    static class RouteNotFoundException extends RuntimeException {

        public RouteNotFoundException(Command command) {
            super("Cannot find a pipeline route for " + command.getClass().getSimpleName() + " command");
        }
    }

}

