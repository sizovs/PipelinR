package an.awesome.pipelinr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
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
    class ConcurrentHashMapWithStats<K, V> extends ConcurrentHashMap<K, V> {
      final AtomicInteger misses = new AtomicInteger(0);

      @Override
      public V computeIfAbsent(K key, Function<? super K, ? extends V> compute) {
        Function<? super K, ? extends V> computeAndTrackMisses =
            (Function<K, V>)
                k -> {
                  misses.incrementAndGet();
                  return compute.apply(k);
                };
        return super.computeIfAbsent(key, computeAndTrackMisses);
      }
    }

    ConcurrentHashMapWithStats<Generic<?>, Type> cache = new ConcurrentHashMapWithStats<>();
    Generic.setCache(cache);

    abstract class IKnowMyType<Foo, Bar> {
      final Generic<Foo> foo1 = new Generic<Foo>(getClass()) {};
      final Generic<Foo> foo2 = new Generic<Foo>(getClass()) {};
      final Generic<Bar> bar1 = new Generic<Bar>(getClass()) {};
      final Generic<Bar> bar2 = new Generic<Bar>(getClass()) {};
    }

    // no reads upon construction -> no cache misses
    IKnowMyType<String, Integer> subj = new IKnowMyType<String, Integer>() {};
    assertThat(cache.misses).hasValue(0);

    // reading the same generic instance twice -> 1 miss
    assertThat(subj.foo1.resolve()).isEqualTo(String.class);
    assertThat(subj.foo1.resolve()).isEqualTo(String.class);
    assertThat(cache.misses).hasValue(1);

    // reading the same generic, but difference instance twice -> still 1 miss
    assertThat(subj.foo2.resolve()).isEqualTo(String.class);
    assertThat(subj.foo2.resolve()).isEqualTo(String.class);
    assertThat(cache.misses).hasValue(1);

    // reading different generic (same instance, twice) -> +1 new miss => 2 total
    assertThat(subj.bar1.resolve()).isEqualTo(Integer.class);
    assertThat(subj.bar1.resolve()).isEqualTo(Integer.class);
    assertThat(cache.misses).hasValue(2);

    // reading the same generic, difference instance  -> no new misses => 2 total
    assertThat(subj.bar2.resolve()).isEqualTo(Integer.class);
    assertThat(subj.bar2.resolve()).isEqualTo(Integer.class);
    assertThat(cache.misses).hasValue(2);
  }

  @Test
  void resolvesParameterizedTypes() {
    abstract class IKnowMyType<T> {
      final Generic<T> foo = new Generic<T>(getClass()) {};
    }
    IKnowMyType<List<String>> subj = new IKnowMyType<List<String>>() {};
    assertThat(subj.foo.resolve()).isEqualTo(List.class);
  }

  @Test
  void resolvesRawTypes() {
    Generic<String> foo = new Generic<String>(getClass()) {};
    assertThat(foo.resolve()).isEqualTo(String.class);
  }
}
