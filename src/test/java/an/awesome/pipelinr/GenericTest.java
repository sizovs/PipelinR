package an.awesome.pipelinr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class GenericTest {

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Test
  void throwIfNonGenericClass() {
    Throwable e =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new Generic(getClass()) {};
            });

    assertThat(e).hasMessage("class an.awesome.pipelinr.Generic isn't parameterized");
  }

  @Test
  void resolvesMultipleGenericTypes() {

    abstract class IKnowMyType<Foo, Bar> {
      final Generic<Foo> foo = new Generic<Foo>(getClass()) {};
      final Generic<Bar> bar = new Generic<Bar>(getClass()) {};
    }

    IKnowMyType<String, Integer> subj = new IKnowMyType<String, Integer>() {};

    assertThat(subj.foo.resolve()).isEqualTo(String.class);
    assertThat(subj.bar.resolve()).isEqualTo(Integer.class);
    assertThat(subj.bar.resolve()).isEqualTo(Integer.class);
    assertThat(subj.bar.resolve()).isEqualTo(Integer.class);

    // caching test
    assertThat(subj.foo.numberOfScans).hasValue(1);
    assertThat(subj.bar.numberOfScans).hasValue(1);
  }
}
