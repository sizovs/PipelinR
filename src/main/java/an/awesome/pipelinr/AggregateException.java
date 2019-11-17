package an.awesome.pipelinr;

import java.util.Collection;
import java.util.Collections;

public class AggregateException extends RuntimeException {

  private final Collection<Throwable> exceptions;

  public AggregateException(Collection<Throwable> exceptions) {
    this.exceptions = Collections.unmodifiableCollection(exceptions);
  }

  public Collection<Throwable> exceptions() {
    return exceptions;
  }

  @Override
  public String getMessage() {
    return exceptions.size() + " exception(s)";
  }
}
