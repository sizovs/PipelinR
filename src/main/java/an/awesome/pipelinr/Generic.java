package an.awesome.pipelinr;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves generic types (like &lt;C&gt; or List&lt;R&gt;) into concrete runtime types in the
 * context of a given class. For example:
 *
 * <pre>
 *   abstract class IKnowMyType&lt;T&gt; {
 *    Generic&lt;T&gt; type = new Generic&lt;T&gt;(getClass()) {};
 *   }
 *   new IKnowMyType&lt;String&gt;() {}.type.resolve(); // String
 * </pre>
 *
 * <p>Resolution happens lazily, upon resolve() invocation, by traversing the class hierarchy via
 * reflection. Since it's a relatively heavy operation, it's performed only once; subsequent calls
 * return the cached result.
 */
public abstract class Generic<C> {

  private static Map<Generic<?>, Type> RESOLVED_GENERICS = new ConcurrentHashMap<>();

  private final Class<?> context;
  private final Type diamond;

  protected Generic(Class<?> context) {
    this.context = context;
    this.diamond = capture();
  }

  private Type capture() {
    Type superclass = getClass().getGenericSuperclass();
    if (!(superclass instanceof ParameterizedType))
      throw new IllegalArgumentException(superclass + " isn't parameterized");

    return ((ParameterizedType) superclass).getActualTypeArguments()[0];
  }

  @SuppressWarnings("unchecked")
  public Class<? super C> resolve() {
    return (Class<? super C>)
        RESOLVED_GENERICS.computeIfAbsent(
            this,
            it -> {
              Mappings mappings = new Scanner().scan(context);
              return mappings.get(diamond);
            });
  }

  @Override
  public boolean equals(Object that) {
    if (this == that) return true;
    if (!(that instanceof Generic)) return false;
    Generic<?> other = (Generic<?>) that;
    return context.equals(other.context) && diamond.equals(other.diamond);
  }

  @Override
  public int hashCode() {
    return 31 * context.hashCode() + diamond.hashCode();
  }

  // for testing
  static void setCache(ConcurrentHashMap<Generic<?>, Type> cache) {
    Generic.RESOLVED_GENERICS = cache;
  }

  /** Walks the class hierarchy, collecting mappings between type variables and actual types. */
  private static class Scanner {

    private final Mappings mappings = new Mappings();

    public Mappings scan(Class<?> clazz) {
      scanSuperclass(clazz);
      scanInterfaces(clazz);
      return mappings;
    }

    private void scanSuperclass(Class<?> clazz) {
      Type superclass = clazz.getGenericSuperclass();
      if (superclass instanceof ParameterizedType) {
        mappings.add((ParameterizedType) superclass);
        scan((Class<?>) ((ParameterizedType) superclass).getRawType());
      } else if (superclass instanceof Class) {
        scan((Class<?>) superclass);
      }
    }

    private void scanInterfaces(Class<?> clazz) {
      for (Type iface : clazz.getGenericInterfaces()) {
        if (iface instanceof ParameterizedType) {
          mappings.add((ParameterizedType) iface);
          scan((Class<?>) ((ParameterizedType) iface).getRawType());
        }
      }
    }
  }

  private static class Mappings {

    // Map of "type variable → actual type".
    // For example: if class MyHandler implements Handler<UserCommand, Result>,
    // and the handler is Handler<C extends Command<R>, R>
    // then we'll map C → UserCommand, R → Result.
    private final Map<TypeVariable<?>, Type> mappings = new HashMap<>();

    /**
     * Adds mappings from type variables (like <C, R>) to actual arguments (like UserCommand,
     * Result).
     */
    public void add(ParameterizedType type) {
      TypeVariable<?>[] generics = ((Class<?>) type.getRawType()).getTypeParameters();
      Type[] concretes = type.getActualTypeArguments();
      for (int i = 0; i < generics.length; i++) {
        mappings.put(generics[i], concretes[i]);
      }
    }

    public Type get(Type type) {
      if (type instanceof TypeVariable) {
        // If it's a type variable like "C" or "R", look up what it was bound to in the current
        // context.
        Type replacement = mappings.get(type);
        // Recursively resolve in case replacement itself is another type variable.
        if (replacement != null) return get(replacement);
      } else if (type instanceof ParameterizedType) {
        // Example: List<String> → List.class
        return ((ParameterizedType) type).getRawType();
      }

      // If it's already a raw class (e.g., String.class), just return it.
      return type;
    }
  }
}
