# PipelinR

[![DevOps By Rultor.com](http://www.rultor.com/b/sizovs/pipelnr)](http://www.rultor.com/p/sizovs/pipelinr)

[![Build Status](https://travis-ci.org/sizovs/PipelinR.svg?branch=master)](https://travis-ci.org/sizovs/PipelinR)
[![Test Coverage](https://codecov.io/gh/sizovs/pipelinr/branch/master/graph/badge.svg)](https://codecov.io/github/sizovs/pipelinr?branch=master)
[![codebeat badge](https://codebeat.co/badges/9f494efc-3c85-45ca-b1a2-52e4f1879f02)](https://codebeat.co/projects/github-com-sizovs-pipelinr-master)


[ ![Download](https://api.bintray.com/packages/eduardsi/maven/pipelinr/images/download.svg) ](https://bintray.com/eduardsi/maven/pipelinr/_latestVersion)

> **PipelinR** is a lightweight command processing pipeline ❍ ⇢ ❍ ⇢ ❍ for your awesome Java app. 


PipelinR has been battle-proven on production, as a service layer in some cool FinTech apps. PipelinR has helped teams switch from a giant service classes handling all use cases to small handlers following single responsibility principle.

## Table of contents
- [How to use](#how-to-use)
- [Commands](#commands)
- [Handlers](#handlers)
- [Pipeline](#pipeline)
- [Spring Example](#spring-example)
- [Async](#async)
- [CQRS](#cqrs)
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
  <version>0.3</version>
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
    compile 'an.awesome:pipelinr:0.3'
}
```

Java version required: 1.8+.

## Commands

**Commands** encapsulate all information needed to perform an action at a later time. You create a command by implementing `Command<R>` interface, where `R` is a return type. If a command has nothing to return, use a built-in `Voidy` return type: 
     
```java
class Ping implements Command<Voidy> {

    public final String host;
    
    public Ping(String host) {
        this.host = host;
    }
}
```   
   
## Handlers    
   
**Handlers** encapsulate command handling logic. You create a handler by implementing `Command.Handler<C, R>` interface, where `C` is a command type and `R` is a return type:

```java
class PingHandler implements Command.Handler<Ping, Voidy> {

    @Override
    public String handle(Ping command) {
        String host = command.host;
        // ... ping logic here ...
        return new Voidy();
    }
}
```

A command must have a single matching handler. By default, PipelinR finds a matching handler by looking at the first generic parameter. For `PingHandler` it is `Ping` command:

```java
class PingHandler implements Command.Handler<Ping, Voidy> {
    // ...
}
```

`PingHandler` will handle `Ping` command, as well as commands that extend `Ping`.

You can override the default matching behavior. By overriding `matches` method, you can select a matching handler at runtime, depending on a condition:

```java
class LocalhostPingHandler implements Command.Handler<Ping, Voidy> {

    @Override
    public boolean matches(Ping command) {
        return command.host.equals("localhost");
    }
}
```

```java
class RemotePingHandler implements Command.Handler<Ping, Voidy> {
    
    @Override
    public boolean matches(Ping command) {
        return !command.host.equals("localhost");
    } 
}
```

## Pipeline
A **pipeline** mediates between commands and handlers. You send commands to the pipeline. When the pipeline receives a command, it sends the command through a sequence of pipeline steps and finally invokes the matching command handler. `Pipelinr` is a default implementation of `Pipeline` interface.

To construct a `Pipeline`, create an instance of `Pipelinr` and provide a list of command handlers:
  
  
```java
Pipeline pipeline = new Pipelinr(() -> Stream.of(new LocalhostPingHandler(), new RemotePingHandler()));
```

Send a command for handling:
 
```java
pipeline.send(new Ping("localhost"));
```  

`Pipelinr` can receive an optional, **ordered list** of custom pipeline steps. Every command will go through the pipeline steps before being handled. Use steps when you want to add extra behavior to command handlers, such as logging, transactions or metrics. 

Pipeline steps must implement `PipelineStep` interface:

```java
// step one (logs a command and a returned result)
class LogInputAndOutput implements PipelineStep {

    @Override
    public <R, C extends Command<R>> R invoke(C command, Next<R> next) {
        // log command
        R response = next.invoke();
        // log response
        return response;
    }
}

// step two (wraps a command handling in a transaction)
class WrapInATransaction implements PipelineStep {

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
Pipeline pipeline = new Pipelinr(
    () -> Stream.of(new LocalhostPingHandler(), new RemotePingHandler()),
    () -> Stream.of(new LogInputAndOutput(), new WrapInATransaction())
);
```

## Spring Example

PipelinR works well with Spring and Spring Boot. 

Start by configuring a `Pipeline`. Create an instance of `Pipelinr` and inject all command handlers and **ordered** pipeline steps via the constructor:

```java
@Configuration
class PipelinrConfiguration {

    @Bean
    Pipeline pipeline(ObjectProvider<Command.Handler> commandHandlers, ObjectProvider<PipelineStep> pipelineSteps) {
        return new Pipelinr(commandHandlers::stream, pipelineSteps::orderedStream);
    }
}
```

Define Spring-managed command handlers:

```java
@Component
class PingHandler implements Command.Handler<Ping, String> {
    // ...
}
```

Optionally, define `Order`-ed pipeline steps:
 
```java
@Component
@Order(1)
class LogInputAndOutput implements PipelineStep {
    // ...
}

@Component
@Order(2)
class WrapInATransaction implements PipelineStep {
    // ...
}
```

Inject `Pipeline` into your application, and start sending commands:

```java
class Application {

    @Autowired
    Pipeline pipeline;

    public void run() {
        String response = pipeline.send(new Ping());
        System.out.println(response); // prints OK        
    }
}

```

You can check fully working Spring Boot demo app [here](https://github.com/sizovs/PipelinR-demo).

## Async

PipelinR works well in async or reactive applications. For example, commands can return `CompletableFuture`:

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
CompletableFuture<String> okInFuture = pipeline.send(new Ping())
```

## CQRS
For CQRS applications you may want to have different pipelines – one for queries, other for commands. `RoutingPipeline` has got you covered. `RoutingPipeline` is a pipeline that can route commands to different pipelines, depending on a condition:

```java
interface Cmd<R> extends Command<R> {
}

interface Query<R> extends Command<R> {
}

Pipeline queriesPipeline = ...  // build a Pipelinr for queries
Pipeline commandsPipeline = ... // build a Pipelinr for commands

Pipeline pipeline = new RoutingPipeline(
        new Route(command -> command instanceof Query, queriesPipeline),
        new Route(command -> command instanceof Cmd, commandsPipeline)
);
```
 

## How to contribute
Just fork the repo and send us a pull request.

## Alternatives
- [MediatR](https://github.com/jbogard/MediatR) – Simple, unambitious mediator implementation in .NET


## Contributors
- [@sizovs](https://github.com/sizovs) as Eduards Sizovs ([Blog](https://sizovs.net)) 
