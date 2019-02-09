package not.your.grandmas.pipelinr.boot;

import not.your.grandmas.pipelinr.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PipelinrConfiguration {

    @Bean
    Pipelinr pipelinr(CommandHandlers commandHandlers, PipelineSteps pipelineSteps) {
        return new Pipelinr(commandHandlers, pipelineSteps);
    }

    @Bean
    PipelineSteps pipelineSteps(ObjectProvider<PipelineStep> providerOfPipelineSteps) {
        return new PipelineSteps(providerOfPipelineSteps::orderedStream);
    }

    @Bean
    CommandHandlers commandHandlers(ObjectProvider<Command.Handler> providerOfCommandHandlers) {
        return new CommandHandlers(providerOfCommandHandlers::orderedStream);
    }




}


