package an.awesome.pipelinr;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import org.junit.jupiter.api.Test;

class AggregateExceptionTest {

  @Test
  void exposesAggregatedExceptions() {
    RuntimeException omg = new RuntimeException("Omg");
    RuntimeException ouch = new RuntimeException("Ouch");

    Collection<Throwable> exceptions = new ArrayList<>();
    exceptions.add(omg);
    exceptions.add(ouch);

    AggregateException aggregateException = new AggregateException(exceptions);

    assertThat(aggregateException.exceptions()).containsExactlyInAnyOrder(omg, ouch);
    assertThat(aggregateException).hasMessage("2 exception(s)");
  }
}
