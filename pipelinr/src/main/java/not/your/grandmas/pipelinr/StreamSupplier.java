package not.your.grandmas.pipelinr;

import java.util.stream.Stream;

@FunctionalInterface
public interface StreamSupplier<T> {

    Stream<T> supply();

}
