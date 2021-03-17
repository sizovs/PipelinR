package an.awesome.pipelinr;

import java.util.stream.Stream;

@FunctionalInterface
public interface StreamSupplier<T> {

  Stream<T> supply();

  default StreamEx<T> supplyEx() {
    return new StreamEx<>(supply());
  }
}
