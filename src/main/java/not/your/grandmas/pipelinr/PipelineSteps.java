package not.your.grandmas.pipelinr;

import not.your.grandmas.pipelinr.PipelineStep.Next;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.function.BiFunction;

import static java.util.Arrays.asList;

public class PipelineSteps {

    private final List<PipelineStep> steps;

    public PipelineSteps(PipelineStep... steps) {
        this.steps = asList(steps);
    }

    public <R> Next<R> foldRight(Next<R> seed, BiFunction<PipelineStep, Next<R>, Next<R>> accumulator) {
        return StreamEx.of(steps).foldRight(seed, accumulator);
    }

}
