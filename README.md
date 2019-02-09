# PipelinR

> **PipelinR** is a lightweight command processing pipeline ❍ ⇢ ❍ ⇢ ❍ for your Java awesome app. 

You can build a flexible service layer with PipelinR.

### Spring Boot example

You should install `pipelinr-boot` via Gradle:
```implementation("not.your.grandmas:pipelinr-boot:1.0.0")```

Warning: pipelinr-boot depends on Spring v5.1.4.

Define a command:
```
class Ping implements Command<String> {
}
```

Define a handler. 
```
@Component
class PingHandler implements Command.Handler<Ping, String> {

    @Override
    public String handle(Ping command) {
        return "OK";
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
