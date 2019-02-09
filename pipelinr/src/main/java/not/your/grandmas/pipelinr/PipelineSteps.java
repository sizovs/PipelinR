package not.your.grandmas.pipelinr;

import not.your.grandmas.pipelinr.PipelineStep.Next;
import one.util.streamex.StreamEx;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiFunction;

public class PipelineSteps {

    private final StreamSupplier<PipelineStep> supplierOfSteps;

    public PipelineSteps(StreamSupplier<PipelineStep> supplierOfStream) {
        this.supplierOfSteps = supplierOfStream;
    }

    public PipelineSteps(PipelineStep... steps) {
        this.supplierOfSteps = () -> Arrays.stream(steps);
    }

    public PipelineSteps(Collection<PipelineStep> steps) {
        this.supplierOfSteps = steps::stream;
    }

    public <R> Next<R> foldRight(Next<R> seed, BiFunction<PipelineStep, Next<R>, Next<R>> accumulator) {
        return StreamEx.of(supplierOfSteps.supply()).foldRight(seed, accumulator);
    }


}
