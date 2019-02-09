# PipelinR

> **PipelinR** is a lightweight command processing pipeline ❍ ⇢ ❍ ⇢ ❍ for your Java awesome app. 

PipelinR is like [MediatR](https://github.com/jbogard/MediatR), but for Java 8+. 

Supports request/response, commands, queries, notifications and events, synchronous and async execution.


### What makes PipelinR awesome
- Ready for production
- Zero dependencies
- Simple, yet flexible
- Well-crafted with 100% test coverage

### Commands

**Commands** encapsulate all information needed to perform an action at a later time. You create a command by implementing `Command<R>` interface, where `R` is a return type. If a command has nothing to return, use a built-in `Voidy` return type: 
     
```
class Ping implements <Command, Voidy> {

    private final String host;
    
    Ping(String host) {
        this.host = host;
    }
    
    public String host() {
        return host;
    }
    
}
```   
   
### Command Handlers    
   
**Command Handlers** encapsulate command handling logic. You create a handler by implementing `Command.Handler<C, R>` interface, where `C` is a command type and `R` is a return type.

```
class PingHandler implements Command.Handler<Ping, Voidy> {

    @Override
    public String handle(Ping command) {
        String host = command.host();
        
        // ... ping logic here ...
        
        return new Voidy();
    }
    
}
```   

A command must have **a single** matching handler. By default, PipelinR finds a matching handler by looking at the first generic parameter. It's `Ping`:

```
class PingHandler implements Command.Handler<Ping, Voidy> {
    ...
}
```

We can override the default matching behavior. By overriding `matches` method, we can select a matching handler at runtime, depending on a condition:
```
class LocalhostPingHandler implements Command.Handler<Ping, Voidy> {
    ...
    @Override
    public boolean matches(Ping command) {
        return command.host().equals("localhost");
    }
```

```
class RemotePingHandler implements Command.Handler<Ping, Voidy> {
    ...
    @Override
    public boolean matches(Ping command) {
        return !command.host().equals("localhost");
    }
```

### Pipeline
A **pipeline** [mediates](https://en.wikipedia.org/wiki/Mediator_pattern) between commands and handlers. We send commands to the pipeline. When the pipeline receives a command, it sends it through a sequence of pipeline steps and finally invokes the matching command handler. PipelinR comes with `Pipeline` interface, implemented by `Pipelinr`.

Pipelinr must receive a list of command handlers:
  
  
```
Pipeline pipeline = new Pipelinr(() -> Stream.of(new LocalhostPingHandler(), new RemotePingHandler()));
```

Now we are ready to send commands for handling:
 
```
pipeline.send(new Ping("localhost"));
```  

Pipelinr can receive an optional, **ordered list** of custom pipeline steps. Every command will go through the pipeline steps before being handled. Use custom steps when you want to log commands, manage transaction, collect metrics, or add [circuit breakers](https://github.com/resilience4j/resilience4j). 

Pipeline steps must implement `PipelineStep` interface:
```
// step one (logs comamnd and the returned result)
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

```
Pipeline pipeline = new Pipelinr(
    () -> Stream.of(new LocalhostPingHandler(), new RemotePingHandler()),
    () -> Stream.of(new LogInputAndOutput(), new WrapInATransaction()
);
```

### Using PipelinR with Spring v5.1.*+ 

Inject all command handlers and pipeline steps known to Spring into the `Pipelinr`. Turn `Pipelinr` into a Spring-managed Bean:
```
@Configuration
class PipelinrConfiguration {

    @Bean
    Pipelinr pipelinr(ObjectProvider<Command.Handler> commandHandlers, ObjectProvider<PipelineStep> pipelineSteps) {
        return new Pipelinr(commandHandlers::orderedStream, pipelineSteps::orderedStream);
    }
}
```

Define a command:
```
class Ping implements Command<String> {
}
```

Define a handler and make it a Spring-managed bean by adding `@Component` annotation:
```
@Component
class PingHandler implements Command.Handler<Ping, String> {

    @Override
    public String handle(Ping command) {
        return "OK";
    }

}
```

Optionally, define `Order`-ed pipeline steps: 
```
@Component
@Order(1)
class LogInputAndOutput implements PipelineStep {

    @Override
    public <R, C extends Command<R>> R invoke(C command, Next<R> next) {
        // log command
        R response = next.invoke();
        // log response
        return response;
    }
}
```

```
@Component
@Order(2)
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

Inject `Pipeline` and start sending commands for processing:
```
@SpringBootApplication
class App implements CommandLineRunner {

    @Autowired
    Pipeline pipeline;

    @Override
    public void run(String... args) {
        String response = pipeline.send(new Ping());
        System.out.println(response); // prints OK        
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}

```

### Async commands
PipelinR works with async or reactive applications. For example, commands can return `CompletableFuture`:

```
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

```
CompletableFuture<String> okInFuture = pipeline.send(new Ping())
```

### Next
- [x] Command flags (e.g. TxFlag)?
- [x] Notifications?