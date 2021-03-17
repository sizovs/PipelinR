package an.awesome.pipelinr;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

class FirstGenericArgOf {

  private final Class<?> aClass;

  FirstGenericArgOf(Class<?> aClass) {
    this.aClass = aClass;
  }

  boolean isAssignableFrom(Class<?> otherClass) {
    Type[] interfaces = aClass.getGenericInterfaces();
    Type genericSuperclass = aClass.getGenericSuperclass();

    ParameterizedType type;
    if (interfaces.length > 0) {
      type = (ParameterizedType) interfaces[0];
    } else {
      type = (ParameterizedType) genericSuperclass;
    }

    Type handlerCommand = type.getActualTypeArguments()[0];
    Class<?> handlerCommandClass;

    if (handlerCommand instanceof ParameterizedType) {
      ParameterizedType parameterized = (ParameterizedType) handlerCommand;
      handlerCommandClass = (Class<?>) parameterized.getRawType();
    } else {
      handlerCommandClass = (Class<?>) handlerCommand;
    }

    return handlerCommandClass.isAssignableFrom(otherClass);
  }
}
