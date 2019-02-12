package an.awesome.pipelinr;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public interface Command<R> {

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

        public CommandTypeInAGeneric(Class<?> aClass) {
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
            Class<?> handlerCommand = (Class<?>) type.getActualTypeArguments()[0];
            return handlerCommand.isAssignableFrom(otherClass);
        }
    }


    interface Router {

        <C extends Command<R>, R> Handler<C, R> route(C command);

    }
}
