package an.awesome.pipelinr;

import static an.awesome.pipelinr.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Pipelinr implements Pipeline {

  private Command.Router router = new ToFirstMatching();

  private StreamSupplier<Command.Middleware> commandMiddlewares = Stream::empty;
  private StreamSupplier<Command.Handler> commandHandlers = Stream::empty;
  private StreamSupplier<Notification.Middleware> notificationMiddlewares = Stream::empty;
  private StreamSupplier<Notification.Handler> notificationHandlers = Stream::empty;
  private Supplier<NotificationHandlingStrategy> notificationHandlingStrategySupplier =
      StopOnException::new;

  public Pipelinr() {}

  public Pipelinr with(CommandHandlers commandHandlers) {
    checkArgument(commandHandlers, "Command handlers must not be null");
    this.commandHandlers = commandHandlers::supply;
    return this;
  }

  public Pipelinr with(NotificationHandlers notificationHandlers) {
    checkArgument(notificationHandlers, "Notification handlers must not be null");
    this.notificationHandlers = notificationHandlers::supply;
    return this;
  }

  public Pipelinr with(Notification.Middlewares middlewares) {
    checkArgument(middlewares, "Middlewares must not be null");
    this.notificationMiddlewares = middlewares::supply;
    return this;
  }

  public Pipelinr with(Command.Middlewares middlewares) {
    checkArgument(middlewares, "Middlewares must not be null");
    this.commandMiddlewares = middlewares::supply;
    return this;
  }

  public Pipelinr with(
      Supplier<NotificationHandlingStrategy> notificationHandlingStrategySupplier) {
    checkArgument(
        notificationHandlingStrategySupplier,
        "Notification handling strategy supplier must not be null");
    this.notificationHandlingStrategySupplier = notificationHandlingStrategySupplier;
    return this;
  }

  public Pipelinr with(Command.Router router) {
    checkArgument(router, "Router must not be null");
    this.router = router;
    return this;
  }

  public <R, C extends Command<R>> R send(C command) {
    checkArgument(command, "Command must not be null");

    Command.Middleware.Next<R> handleCommand = new HandleCommand<>(command);
    return commandMiddlewares
        .supplyEx()
        .foldRight(handleCommand, (step, next) -> () -> step.invoke(command, next))
        .invoke();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <N extends Notification> void send(N notification) {
    checkArgument(notification, "Notification must not be null");

    List<Runnable> runnableNotifications =
        notificationHandlers
            .supply()
            .filter(it -> it.matches(notification))
            .map(
                it -> {
                  Notification.Middleware.Next handleNotification = () -> it.handle(notification);
                  return (Runnable)
                      () ->
                          notificationMiddlewares
                              .supplyEx()
                              .foldRight(
                                  handleNotification,
                                  (step, next) -> () -> step.invoke(notification, next))
                              .invoke();
                })
            .collect(toList());

    NotificationHandlingStrategy notificationHandlingStrategy =
        notificationHandlingStrategySupplier.get();
    notificationHandlingStrategy.handle(runnableNotifications);
  }

  private class HandleCommand<R, C extends Command<R>> implements Command.Middleware.Next<R> {

    private final C command;

    public HandleCommand(C command) {
      this.command = command;
    }

    @Override
    public R invoke() {
      Command.Handler<C, R> handler = router.route(command);
      return handler.handle(command);
    }
  }

  private class ToFirstMatching implements Command.Router {

    @Override
    @SuppressWarnings("unchecked")
    public <C extends Command<R>, R> Command.Handler<C, R> route(C command) {
      List<Command.Handler> matchingHandlers =
          commandHandlers.supply().filter(handler -> handler.matches(command)).collect(toList());

      boolean noHandlers = matchingHandlers.isEmpty();
      if (noHandlers) {
        throw new CommandHandlerNotFoundException(command);
      }

      boolean multipleHandlers = matchingHandlers.size() > 1;
      if (multipleHandlers) {
        throw new CommandHasMultipleHandlersException(command, matchingHandlers);
      }

      return matchingHandlers.get(0);
    }
  }
}
