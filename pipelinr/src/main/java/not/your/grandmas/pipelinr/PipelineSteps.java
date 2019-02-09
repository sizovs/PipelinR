package not.your.grandmas.pipelinr;

import not.your.grandmas.pipelinr.PipelineStep.Next;
import one.util.streamex.StreamEx;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class PipelineSteps {

    private final Supplier supplierOfSteps;

    public PipelineSteps(Supplier supplier) {
        this.supplierOfSteps = supplier;
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

    @FunctionalInterface
    public interface Supplier {
        Stream<PipelineStep> supply();
    }

}
