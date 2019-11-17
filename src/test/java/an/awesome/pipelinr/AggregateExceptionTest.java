package an.awesome.pipelinr;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

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