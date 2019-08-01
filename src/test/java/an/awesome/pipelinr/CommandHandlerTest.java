package an.awesome.pipelinr;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CommandHandlerTest {

    @Test
    void resolvesHandlersWithAGenericCommandType() {
        Pipeline pipeline = new Pipelinr(() -> Stream.of(new HandlerWithAGenericCommandType()));

        String results = pipeline.send(new Foo<>(new Bar()));
        assertThat(results).isEqualTo("Bar");
    }

    class Bar implements Command<String> {

    }

    class Foo<C extends Command<R>, R> implements Command<R> {
        C wrappee;
        Foo(C wrappee) {
            this.wrappee = wrappee;
        }
    }

    class HandlerWithAGenericCommandType<C extends Command<R>, R> implements Command.Handler<Foo<C, R>, R> {

        @SuppressWarnings("unchecked")
        @Override
        public R handle(Foo command) {
            return (R) command.wrappee.getClass().getSimpleName();
        }
    }


    @Test
    void handlesCommandsThatAreSubtypesOfAGenericArgument() {
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