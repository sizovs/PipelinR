package not.your.grandmas.pipelinr;

import one.util.streamex.StreamEx;

import java.util.List;

import static java.util.Arrays.asList;

public class PipelineSteps {

    private final List<PipelineStep> steps;

    public PipelineSteps(PipelineStep... steps) {
        this.steps = asList(steps);
    }

    public StreamEx<PipelineStep> stream() {
        return StreamEx.of(steps);
    }

}
