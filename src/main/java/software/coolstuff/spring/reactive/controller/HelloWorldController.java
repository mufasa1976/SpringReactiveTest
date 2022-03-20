package software.coolstuff.spring.reactive.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping(HelloWorldController.ENDPOINT)
public class HelloWorldController {
  public static final String ENDPOINT = "api/v1/hello-world";

  @GetMapping
  public Mono<String> helloWorld() {
    return Mono.just("Hello World!");
  }

  @GetMapping(params = "repeat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<String> helloWorldRepeated(@RequestParam(name = "waitSeconds", defaultValue = "1") long waitSeconds) {
    return Flux.just("Hello World!")
               .repeat()
               .delayElements(Duration.ofSeconds(waitSeconds));
  }
}
