package an.awesome.pipelinr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class GenericTest {

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Test
  void throwsIfNotParameterized() {
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

  @Test
  void resolvesParameterizedTypes() {
    abstract class IKnowMyType<T> {
      final Generic<T> foo = new Generic<T>(getClass()) {};
    }
    IKnowMyType<List<String>> subj = new IKnowMyType<List<String>>() {};
    assertThat(subj.foo.resolve()).isEqualTo(List.class);
  }
}
