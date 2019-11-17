# PipelinR

[![DevOps By Rultor.com](http://www.rultor.com/b/sizovs/pipelnr)](http://www.rultor.com/p/sizovs/pipelinr)

[![Build Status](https://travis-ci.org/sizovs/PipelinR.svg?branch=master)](https://travis-ci.org/sizovs/PipelinR)
[![Test Coverage](https://codecov.io/gh/sizovs/pipelinr/branch/master/graph/badge.svg)](https://codecov.io/github/sizovs/pipelinr?branch=master)
[![codebeat badge](https://codebeat.co/badges/9f494efc-3c85-45ca-b1a2-52e4f1879f02)](https://codebeat.co/projects/github-com-sizovs-pipelinr-master)


[ ![Download](https://api.bintray.com/packages/eduardsi/maven/pipelinr/images/download.svg) ](https://bintray.com/eduardsi/maven/pipelinr/_latestVersion)

> **PipelinR** is a lightweight command processing pipeline ❍ ⇢ ❍ ⇢ ❍ for your awesome Java app. 


PipelinR has been battle-proven on production, as a service layer in some cool FinTech apps. PipelinR has helped teams switch from a giant service classes handling all use cases to small handlers following single responsibility principle.

💡 Join [Effective Java Software Design](https://devchampions.com/training/java) course to learn more about building great Java enterprise applications.

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

PipelinR has no dependencies. All you need is a single 15KB library:

Maven:

```xml
<dependency>
  <groupId>an.awesome</groupId>
  <artifactId>pipelinr</artifactId>
  <version>0.5</version>
</dependency>

<repositories>
  <repository>
    <id>central</id>
    <name>bintray</name>
    <url>http://jcenter.bintray.com</url>
  </repository>
</repositories>
```

Gradle:

```groovy
repositories {
    jcenter()
}

dependencies {
    compile 'an.awesome:pipelinr:0.5'
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

`Pipelinr` can receive an optional, **ordered list** of custom middlewares. Every command will go through the middlewares before being handled. Use middlewares when you want to add extra behavior to command handlers, such as logging, transactions or metrics:

```java
// middleware that logs every command and the result it returns
class Loggable implements Command.Middleware {

    @Override
    public <R, C extends Command<R>> R invoke(C command, Next<R> next) {
        // log command
        R response = next.invoke();
        // log response
        return response;
    }
}

// middleware that wraps a command in a transaction
class Transactional implements Command.Middleware {

    @Override
    public <R, C extends Command<R>> R invoke(C command, Next<R> next) {
        // start tx
        R response = next.invoke();
        // end tx
        return response;
    }
}
```

In the following pipeline, every command and its response will be logged, plus commands will be wrapped in a transaction:

```java
Pipeline pipeline = new Pipelinr()
    .with(() -> Stream.of(new Pong())),
    .with(() -> Stream.of(new Loggable(), new Transactional()))
);
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
* `an.awesome.pipelinr.StopOnException` (default)
* `an.awesome.pipelinr.ContinueOnException`
* `an.awesome.pipelinr.Async`
* `an.awesome.pipelinr.ParallelNoWait`
* `an.awesome.pipelinr.ParallelWhenAny`
* `an.awesome.pipelinr.ParallelWhenAll`

See each class' JavaDocs for the details.

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
    Pipeline pipeline(ObjectProvider<Command.Handler> commandHandlers, ObjectProvider<Middleware> middlewares) {
        return new Pipelinr()
          .with(commandHandlers::stream)
          .with(middlewares::orderedStream);
    }
}
```

Define Spring-managed command handlers:

```java
@Component
class Pong implements Command.Handler<Ping, String> {
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

Inject `Pipeline` into your application, and start sending commands:

```java
class Application {

    @Autowired
    Pipeline pipeline;

    public void run() {
        String response = new Ping("localhost").execute(pipeline);
        System.out.println(response); 
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
- [@sizovs](https://github.com/sizovs) as Eduards Sizovs ([Blog](https://sizovs.net)) 
