package not.your.grandmas.pipelinr.boot.tests;

import not.your.grandmas.pipelinr.Command;
import not.your.grandmas.pipelinr.Pipeline;
import not.your.grandmas.pipelinr.PipelineStep;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(SpringExtension.class)
@SpringBootTest
public class PipelinrSpringBootTest {

    @Autowired
    Pipeline pipeline;

    @Test
    public void supportsOrderedPipelineSteps() {
        // given
        Ping ping = new Ping();

        // when
        pipeline.send(ping);

        // then
        assertThat(ping.pipelineOrder).containsExactly(1, 2, 3);
    }

}

class Ping implements Command<String> {

    List<Integer> pipelineOrder = new ArrayList<>();

}

@Component
class PingHandler implements Command.Handler<Ping, String> {

    @Override
    public String handle(Ping command) {
        return "Pong";
    }

}

@Component
@Order(1)
class StepOne implements PipelineStep {

    @Override
    public <R, C extends Command<R>> R invoke(C command, Next<R> next) {
        Ping ping = (Ping) command;
        ping.pipelineOrder.add(1);
        return next.invoke();
    }
}

@Component
@Order(3)
class StepThree implements PipelineStep {

    @Override
    public <R, C extends Command<R>> R invoke(C command, Next<R> next) {
        Ping ping = (Ping) command;
        ping.pipelineOrder.add(3);
        return next.invoke();
    }

}

@Component
@Order(2)
class StepTwo implements PipelineStep {

    @Override
    public <R, C extends Command<R>> R invoke(C command, Next<R> next) {
        Ping ping = (Ping) command;
        ping.pipelineOrder.add(2);
        return next.invoke();
    }

}

@SpringBootApplication
class App {


}




