# PipelinR

> **PipelinR** is a lightweight command processing pipeline ❍ ⇢ ❍ ⇢ ❍ for your Java awesome app. 

It's like [MediatR](https://github.com/jbogard/MediatR), but for Java – great for building a flexible service layer.


### What makes PipelinR awesome
- 🚀 Ready for production
- 🚀 Zero dependencies
- 🚀 Simple, yet flexible
- 🚀 Well-crafted with 100% test coverage

### Understanding elements of PipelinR

#### ✅ Commands 

**Commands** encapsulate all information needed to perform an action at a later time. You create a command by implementing `Command` interface, where `R` is a return type: 

```
class Ping implements <Command, R> {

    private final String host;
    private final int times;
    
    Ping(String host, int times) {
        this.host = host;
        this.times = times;
    }
    
    public String host() {
        return host;
    }
    
    public int times() {
        return times;
    }
    
}
```

If a command has nothing to return, use a built-in `Voidy` return type.   
   
#### ✅ Command Handlers    
   
**Command Handlers** encapsulate command handling logic. Every command must have a matching handler. You create a handler by implementing `Command.Handler` interface.

- **Pipeline** receives a command, sends it through a sequence of `PipelineStep`s and finally invokes the matching command handler.
 
- **Pipelinr** is an implementation of the Pipeline
  

### How to use PipelinR with Spring Framework (5.1.4+) 

✅ Install PipelinR via Gradle:
```
implementation("not.your.grandmas:pipelinr:1.0.0")
```

✅ Configure Pipelinr:
```
@Configuration
class PipelinrConfiguration {

    @Bean
    Pipelinr pipelinr(ObjectProvider<Command.Handler> commandHandlers, ObjectProvider<PipelineStep> pipelineSteps) {
        return new Pipelinr(commandHandlers::orderedStream, pipelineSteps::orderedStream);
    }
}
```

✅ Define a command:
```
class Ping implements Command<String> {
}
```

✅ Define a handler:
```
@Component
class PingHandler implements Command.Handler<Ping, String> {

    @Override
    public String handle(Ping command) {
        return "OK";
    }

}
```

✅ Optionally, define pipeline steps:
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

✅ Send the command to the pipeline for processing:
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
