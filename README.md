# PipelinR

> **PipelinR** is a lightweight command processing pipeline ❍ ⇢ ❍ ⇢ ❍ for your Java awesome app. 

You can build a flexible service layer with PipelinR.

### How to use PipelinR with Spring Framework (5.1.4+) 

✅ Install `pipelinr-boot` via Gradle:
```
implementation("not.your.grandmas:pipelinr:1.0.0")
```

✅ Configure Pipelinr:
```
@Configuration
class PipelinrConfiguration {

    @Bean
    Pipelinr pipelinr(CommandHandlers commandHandlers, PipelineSteps pipelineSteps) {
        return new Pipelinr(commandHandlers, pipelineSteps);
    }

    @Bean
    PipelineSteps pipelineSteps(ObjectProvider<PipelineStep> providerOfPipelineSteps) {
        return new PipelineSteps(providerOfPipelineSteps::orderedStream);
    }

    @Bean
    CommandHandlers commandHandlers(ObjectProvider<Command.Handler> providerOfCommandHandlers) {
        return new CommandHandlers(providerOfCommandHandlers::orderedStream);
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


### What makes PipelinR awesome
- [x] Ready for production
- [x] Zero dependencies
- [x] Simple, yet flexible
- [x] Well-crafted with 100% test coverage

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
