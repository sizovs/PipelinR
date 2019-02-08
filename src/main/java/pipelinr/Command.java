package pipelinr;

import com.google.common.reflect.TypeToken;

public interface Command<R> {

    interface Handler<C extends Command<R>, R> {

        R handle(C command);

        default boolean matches(C command) {
            TypeToken<C> commandType = new TypeToken<C>(getClass()) {
            };

            return commandType.isSupertypeOf(command.getClass());
        }
    }



}
