package an.awesome.pipelinr;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommandTest {

  @Test
  void delegatesToPipeline(@Mock Pipeline cmdPipeline) {
    Command<Voidy> cmd = new Command<Voidy>() {};
    cmd.execute(cmdPipeline);

    verify(cmdPipeline).send(cmd);
  }
}
