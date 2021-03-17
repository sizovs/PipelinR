package an.awesome.pipelinr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class PreconditionsTest {

  @Test
  void throwsIfArgumentIsNull() {
    Throwable e =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              Preconditions.checkArgument(null, "Shit has happened");
            });

    assertThat(e).hasMessage("Shit has happened");
  }

  @Test
  void throwsIfArgumentIsFalse() {
    Throwable e =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              Preconditions.checkArgument(false, "Shit has happened");
            });

    assertThat(e).hasMessage("Shit has happened");
  }

  @Test
  void returnsAnArgumentIfTheArgumentIsAValidObject() {
    // given
    Object checkedObject = new Object();

    // when
    Object returnedObject = Preconditions.checkArgument(checkedObject, "OK");

    // then
    assertThat(returnedObject).isEqualTo(checkedObject);
  }

  @Test
  void passesIfAnArgumentIsTruthy() {
    assertDoesNotThrow(() -> Preconditions.checkArgument(true, "OK"));
  }
}
