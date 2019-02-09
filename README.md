# PipelinR

> **PipelinR** is a lightweight command processing pipeline ❍ ⇢ ❍ ⇢ ❍ for your Java awesome app. 

PipelinR is like [MediatR](https://github.com/jbogard/MediatR), but for Java. 

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
A **pipeline** mediates between commands and handlers. We send commands to the pipeline. When the pipeline receives a command, it sends it through a sequence of pipeline steps and finally invokes the matching command handler. PipelinR comes with `Pipeline` interface, implemented by `Pipelinr`.

Pipelinr must know about the command handlers:
  
  
```
Pipeline pipeline = new Pipelinr(() -> Stream.of(new LocalhostPingHandler(), new RemotePingHandler()));
```

We are ready to send commands to the pipeline:
 
```
pipeline.send(new Ping("localhost"));
```  

### How to use PipelinR with Spring Framework (5.1.4+) 

Install PipelinR via Gradle:
```
implementation("not.your.grandmas:pipelinr:1.0.0")
```

Configure Pipelinr:
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

Define a handler:
```
@Component
class PingHandler implements Command.Handler<Ping, String> {

    @Override
    public String handle(Ping command) {
        return "OK";
    }

}
```

Optionally, define pipeline steps:
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

Send the command to the pipeline for processing:
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

### Next
- [x] Command flags (e.g. TxFlag)?
- [x] Notifications?

### Requirements
- [x] Java 8+

### Spring demo


### Guice demo

### Dagger

### HK2


### Raw demo
