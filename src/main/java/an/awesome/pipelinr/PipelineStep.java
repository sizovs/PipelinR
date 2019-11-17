package an.awesome.pipelinr;

@FunctionalInterface
@Deprecated
public interface PipelineStep {

    <R, C extends Command<R>> R invoke(C command, Next<R> next);


    @Deprecated
    interface Next<T> {

        T invoke();

    }

}
