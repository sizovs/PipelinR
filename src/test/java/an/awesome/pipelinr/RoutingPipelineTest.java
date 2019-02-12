package an.awesome.pipelinr;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RoutingPipelineTest {

    @Test
    public void throwsRouteNotFoundException() {
        // given
        Pipeline pipeline = new RoutingPipeline();

        // and
        Cmd.Impl cmd = new Cmd.Impl();

        // when
        Throwable e = assertThrows(RoutingPipeline.RouteNotFoundException.class, () -> {
           pipeline.send(cmd);
        });

        assertThat(e).hasMessage("Cannot find a pipeline route for Impl command");

    }

    @Test
    public void routesToAPipelineDependingOnACondition(@Mock Pipeline cmdPipeline, @Mock Pipeline queryPipeline) {
        // given
        Pipeline pipeline = new RoutingPipeline(
                new RoutingPipeline.Route(command -> command instanceof Query, queryPipeline),
                new RoutingPipeline.Route(command -> command instanceof Cmd, cmdPipeline)
        );

        // and
        Cmd.Impl cmd = new Cmd.Impl();
        Query.Impl query = new Query.Impl();

        // when
        pipeline.send(cmd);
        pipeline.send(query);

        // then
        verify(cmdPipeline).send(cmd);
        verify(queryPipeline).send(query);
    }




    interface Cmd<R> extends Command<R> {
        class Impl implements Cmd<Voidy> {
        }
    }

    interface Query<R> extends Command<R> {
        class Impl implements Query<Voidy> {
        }
    }

}


