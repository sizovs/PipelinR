package not.your.grandmas.pipelinr;

import java.util.stream.Stream;

@FunctionalInterface
public interface Multiple<T> {

    Stream<T> supply();


}
