package not.your.grandmas.pipelinr.boot;

import not.your.grandmas.pipelinr.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class PipelinrConfiguration {

    @Bean
    Pipelinr pipelinr(ObjectProvider<PipelineStep> providerOfPipelineSteps, ObjectProvider<Command.Handler> providerOfCommandHandlers) {
        return new Pipelinr(providerOfCommandHandlers::orderedStream, providerOfPipelineSteps::orderedStream);
    }

}


