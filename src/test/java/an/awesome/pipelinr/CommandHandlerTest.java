package an.awesome.pipelinr;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CommandHandlerTest {


    @Test
    public void handlesCommandsThatAreSubtypesOfAGenericArgument() {
        // given
        Ping.Handler pingHandler = new Ping.Handler();
        NotAPing.Handler notAPingHandler = new NotAPing.Handler();
        Pipeline pipeline = new Pipelinr(() -> Stream.of(pingHandler, notAPingHandler));

        // and
        Ping ping = new Ping();
        SmartPing smartPing = new SmartPing();
        NotAPing notAPing = new NotAPing();

        // when
        pipeline.send(ping);
        pipeline.send(smartPing);
        pipeline.send(notAPing);

        // then
        assertThat(pingHandler.handled).containsOnly(ping, smartPing);
        assertThat(notAPingHandler.handled).containsOnly(notAPing);
    }

    static class Ping implements Command<Voidy> {

        static class Handler implements Command.Handler<Ping, Voidy> {

            private Collection<Command> handled = new ArrayList<>();

            @Override
            public Voidy handle(Ping command) {
                handled.add(command);
                return new Voidy();
            }
        }
    }

    static class SmartPing extends Ping {

    }

    static class NotAPing implements Command<Voidy> {
        static class Handler implements Command.Handler<NotAPing, Voidy> {

            private Collection<Command> handled = new ArrayList<>();

            @Override
            public Voidy handle(NotAPing command) {
                handled.add(command);
                return new Voidy();
            }
        }
    }


}