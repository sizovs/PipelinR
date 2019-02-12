package an.awesome.pipelinr;

@FunctionalInterface
public interface PipelineStep {

    <R, C extends Command<R>> R invoke(C command, Next<R> next);


    interface Next<T> {

        T invoke();

    }

}
