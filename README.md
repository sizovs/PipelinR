# PipelinR

> **PipelinR** is a lightweight command processing pipeline ❍ ⇢ ❍ ⇢ ❍ for your Java awesome app. 

You can build a flexible service layer with PipelinR.


### What makes PipelinR awesome
- [x] Ready for production
- [x] Zero dependencies
- [x] Simple, yet flexible
- [x] Well-crafted with 100% test coverage

### Understanding elements of PipelinR

- **Command** encapsulates all information needed to perform an action at a later time. For example – `Ping`, `CreateAUser`, `SendEmail` are commands.
   
- **Command.Handler** encapsulates command handling logic. Every command must have a matching handler. For example `PingHandler`, `CreateAUserHandler`, `SendEmailHandler` are command handlers.

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
