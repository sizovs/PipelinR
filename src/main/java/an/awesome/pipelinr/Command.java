package an.awesome.pipelinr;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public interface Command<R> {

    default R execute(Pipeline pipeline) {
        return pipeline.send(this);
    }

    interface Handler<C extends Command<R>, R> {

        R handle(C command);

        default boolean matches(C command) {
            Class handlerType = getClass();
            Class commandType = command.getClass();
            return new CommandTypeInAGeneric(handlerType).isAssignableFrom(commandType);
        }
    }

    class CommandTypeInAGeneric {

        private final Class<?> aClass;

        CommandTypeInAGeneric(Class<?> aClass) {
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


    interface Router {

        <C extends Command<R>, R> Handler<C, R> route(C command);

    }
}
