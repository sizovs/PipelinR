# PipelinR

[![Build Status](https://github.com/sizovs/pipelinr/actions/workflows/build.yml/badge.svg)](https://github.com/sizovs/pipelinr/actions/workflows/build.yml)
[![Test Coverage](https://codecov.io/gh/sizovs/pipelinr/branch/master/graph/badge.svg)](https://codecov.io/github/sizovs/pipelinr?branch=master)
[![libs.tech recommends](https://libs.tech/project/169682577/badge.svg)](https://libs.tech/project/169682577/pipelinr)
[![codebeat badge](https://codebeat.co/badges/9f494efc-3c85-45ca-b1a2-52e4f1879f02)](https://codebeat.co/projects/github-com-sizovs-pipelinr-master)
![Maven Central Version](https://img.shields.io/maven-central/v/net.sizovs/pipelinr)


> **PipelinR** is a lightweight command processing pipeline ❍ ⇢ ❍ ⇢ ❍ for your awesome Java app.

PipelinR has been battle-proven on production as a service layer for some cool FinTech apps. PipelinR has helped teams switch from giant service classes handling all use cases to small handlers, each following the single responsibility principle. It's similar to a popular [MediatR](https://github.com/jbogard/MediatR) .NET library.

⚡ Tested and works with plain Java, Kotlin, Spring, and Jakarta EE.

## Table of contents
- [How to use](#how-to-use)
- [Commands](#commands)
- [Handlers](#handlers)
- [Pipeline](#pipeline)
- [Notifications](#notifications)
- [Spring Example](#spring-example)
- [Async](#async)
- [How to contribute](#how-to-contribute)
- [Alternatives](#alternatives)
- [Contributors](#contributors)

## How to use

PipelinR has no dependencies. All you need is a single library:

Maven:

```xml
<dependency>
  <groupId>net.sizovs</groupId>
  <artifactId>pipelinr</artifactId>
  <version>0.9</version>
</dependency>
```

Gradle:

```groovy
dependencies {
    compile 'net.sizovs:pipelinr:0.9'
}
```

Java version required: 1.8+.

## Commands

**Commands** is a request that can return a value. The `Ping` command below returns a string:

```java
class Ping implements Command<String> {

    public final String host;

    public Ping(String host) {
        this.host = host;
    }
}
```

If a command has nothing to return, you can use a built-in `Voidy` return type:

```java
class Ping implements Command<Voidy> {

    public final String host;

    public Ping(String host) {
        this.host = host;
    }
}
```

## Handlers

For every command you must define a **Handler**, that knows how to handle the command.

Create a handler by implementing `Command.Handler<C, R>` interface, where `C` is a command type and `R` is a return type. Handler's return type must match command's return type:

```java
class Pong implements Command.Handler<Ping, String> {

    @Override
    public String handle(Ping command) {
        return "Pong from " + command.host;
    }
}
```

## Pipeline
A **pipeline** mediates between commands and handlers. You send commands to the pipeline. When the pipeline receives a command, it sends the command through a sequence of middlewares and finally invokes the matching command handler. `Pipelinr` is a default implementation of `Pipeline` interface.

To construct a `Pipeline`, create an instance of `Pipelinr` and provide a list of command handlers:


```java
Pipeline pipeline = new Pipelinr()
    .with(
        () -> Stream.of(new Pong())
    );
```

Send a command for handling:

```java
pipeline.send(new Ping("localhost"));
```

since v0.4, you can execute commands more naturally:

```java
new Ping("localhost").execute(pipeline);
```

`Pipelinr` can receive an optional, **ordered list** of custom middlewares. Every command will go through the middlewares before being handled. Use middlewares when you want to add extra behavior to command handlers, such as validation, logging, transactions, or metrics:

```java
// command validation + middleware

interface CommandValidator<C extends Command<R>, R> {
    void validate(C command);

    default boolean matches(C command) {
        TypeToken<C> typeToken = new TypeToken<C>(getClass()) { // available in Guava 12+.
        };

        return typeToken.isSupertypeOf(command.getClass());
    }
}

class ValidationMiddleware implements Command.Middleware {
   private final ObjectProvider<CommandValidator> validators; // requires Spring 5+. For older versions, use BeanFactory.

   ValidationMiddleware(ObjectProvider<CommandValidator> validators) {
      this.validators = validators;
    }

    @Override
    public <R, C extends Command<R>> R invoke(C command, Next<R> next) {
        validators.stream().filter(v -> v.matches(command)).findFirst().ifPresent(v -> v.validate(command));
        return next.invoke();
    }
}
```

```java
// middleware that logs every command and the result it returns
class LoggingMiddleware implements Command.Middleware {

    @Override
    public <R, C extends Command<R>> R invoke(C command, Next<R> next) {
        // log command
        R response = next.invoke();
        // log response
        return response;
    }
}

// middleware that wraps a command in a transaction
class TxMiddleware implements Command.Middleware {

    @Override
    public <R, C extends Command<R>> R invoke(C command, Next<R> next) {
        // start tx
        R response = next.invoke();
        // end tx
        return response;
    }
}
```

In the following pipeline, every command and its response will be logged, it will be wrapped in a transaction, then validated:

```java
Pipeline pipeline = new Pipelinr()
    .with(() -> Stream.of(new Pong()))
    .with(() -> Stream.of(new LoggingMiddleware(), new TxMiddleware(), new ValidationMiddleware(...)));
```

By default, command handlers are being resolved using generics. By overriding command handler's `matches` method, you can dynamically select a matching handler:

```java
class LocalhostPong implements Command.Handler<Ping, String> {

    @Override
    public boolean matches(Ping command) {
        return command.host.equals("localhost");
    }

}
```

```java
class NonLocalhostPong implements Command.Handler<Ping, String> {

    @Override
    public boolean matches(Ping command) {
        return !command.host.equals("localhost");
    }
}
```

## Notifications

Since version `0.5`, PipelinR supports Notifications, dispatched to multiple handlers.

For notifications, first create your notification message:

```java
class Ping implements Notification {
}
```

Next, create zero or more handlers for your notification:

```java
public class Pong1 implements Notification.Handler<Ping> {

    @Override
    public void handle(Ping notification) {
      System.out.printn("Pong 1");
    }
}

public class Pong2 implements Notification.Handler<Ping> {

    @Override
    public void handle(Ping notification) {
      System.out.printn("Pong 2");
    }
}
```

Finally, send notification to the pipeline:

```java
new Ping().send(pipeline);
```

💡 Remember to provide notification handlers to PipelinR:
```java
new Pipelinr()
  .with(
    () -> Stream.of(new Pong1(), new Pong2())
  )
```

### Notification middlewares

Notifications, like commands, support middlewares. Notification middlewares will run before every notification handler:

```java
class Transactional implements Notification.Middleware {

    @Override
    public <N extends Notification> void invoke(N notification, Next next) {
        // start tx
        next.invoke();
        // stop tx
    }
}

new Pipelinr().with(() -> Stream.of(new Transactional()))
```

### Notification handling strategies
The default implementation loops through the notification handlers and awaits each one. This ensures each handler is run after one another.

Depending on your use-case for sending notifications, you might need a different strategy for handling the notifications, such running handlers in parallel.

PipelinR supports the following strategies:
* `an.awesome.pipelinr.StopOnException` runs each notification handler after one another; returns when all handlers are finished or an exception has been thrown; in case of an exception, any handlers after that will not be run; **this is a default strategy**.
* `an.awesome.pipelinr.ContinueOnException` runs each notification handler after one another; returns when all handlers are finished; in case of any exception(s), they will be captured in an AggregateException.
* `an.awesome.pipelinr.Async` runs all notification handlers asynchronously; returns when all handlers are finished; in case of any exception(s), they will be captured in an AggregateException.
* `an.awesome.pipelinr.ParallelNoWait` runs each notification handler in a thread pool; returns immediately and does not wait for any handlers to finish; cannot capture any exceptions.
* `an.awesome.pipelinr.ParallelWhenAny` runs each notification handler in a thread pool; returns when any thread (handler) is finished; all exceptions that happened before returning are captured in an AggregateException.
* `an.awesome.pipelinr.ParallelWhenAll` runs each notification handler in a thread pool; returns when all threads (handlers) are finished; in case of any exception(s), they are captured in an AggregateException.

You can override default strategy via:
```java
new Pipelinr().with(new ContinueOnException());
```

## Spring Example

PipelinR works well with Spring and Spring Boot.

Start by configuring a `Pipeline`. Create an instance of `Pipelinr` and inject all command handlers and **ordered** middlewares via the constructor:

```java
@Configuration
class PipelinrConfiguration {

    @Bean
    Pipeline pipeline(ObjectProvider<Command.Handler> commandHandlers, ObjectProvider<Notification.Handler> notificationHandlers, ObjectProvider<Command.Middleware> middlewares) {
        return new Pipelinr()
          .with(commandHandlers::stream)
          .with(notificationHandlers::stream)
          .with(middlewares::orderedStream);
    }
}
```

Define a command:

```java
class Wave implements Command<String> {
}
```

Define a handler and annotate it with `@Component` annotation:

```java
@Component
class WaveBack implements Command.Handler<Wave, String> {
    // ...
}
```


Optionally, define `Order`-ed middlewares:

```java
@Component
@Order(1)
class Loggable implements Command.Middleware {
    // ...
}

@Component
@Order(2)
class Transactional implements Command.Middleware {
    // ...
}
```

To use notifications, define a notification:

```java
class Ping implements Notification {
}
```

Define notification handlers and annotate them with `@Component` annotation:

```java
@Component
class Pong1 implements Notification.Handler<Ping> {
    // ...
}

@Component
class Pong2 implements Notification.Handler<Ping> {
    // ...
}
```

> Remember that notifications, like commands, also support [Middlewares](#notification-middlewares).

We're ready to go! Inject `Pipeline` into your application, and start sending commands or notifications:

```java
class Application {

    @Autowired
    Pipeline pipeline;

    public void run() {
        String response = new Wave().execute(pipeline);
        System.out.println(response);

        // ... or

        new Ping().send(pipeline); // should trigger Pong1 and Pong2 notification handlers

    }
}

```

## Async

PipelinR works well in async or reactive applications. For example, a command can return `CompletableFuture`:

```java
class AsyncPing implements Command<CompletableFuture<String>> {

    @Component
    static class Handler implements Command.Handler<AsyncPing, CompletableFuture<String>> {

        @Override
        public CompletableFuture<String> handle(AsyncPing command) {
            return CompletableFuture.completedFuture("OK");
        }
    }
}
```

Sending `AsyncPing` to the pipeline returns `CompletableFuture`:

```java
CompletableFuture<String> okInFuture = new Ping().execute(pipeline);
```


## How to contribute
Just fork the repo and send us a pull request.

## Alternatives
- [MediatR](https://github.com/jbogard/MediatR) – Simple, unambitious mediator implementation in .NET


## Contributors
- Eduards Sizovs: [Blog](https://sizovs.net) ⋅ [Twitter](https://twitter.com/eduardsi) ⋅ [GitHub](https://github.com/sizovs)
