package software.coolstuff.spring.reactive.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(HelloWorldController.ENDPOINT)
public class HelloWorldController {
  public static final String ENDPOINT = "api/v1/hello-world";

  @GetMapping
  public Mono<String> helloWorld() {
    return Mono.just("Hello World!");
  }
}
