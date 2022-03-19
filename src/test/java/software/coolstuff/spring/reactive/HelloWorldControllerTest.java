package software.coolstuff.spring.reactive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import software.coolstuff.spring.reactive.config.WebfluxConfiguration;
import software.coolstuff.spring.reactive.controller.HelloWorldController;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = HelloWorldController.class)
@Import(WebfluxConfiguration.class)
class HelloWorldControllerTest {
  private static final String TEXT_PLAIN_UTF8 = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8";

  @Autowired
  private WebTestClient webClient;

  @Test
  @DisplayName("GET /" + HelloWorldController.ENDPOINT + " and expect 'Hello World!'")
  void helloWorld() {
    webClient.get().uri("/" + HelloWorldController.ENDPOINT)
             .exchange()
             .expectStatus().isOk()
             .expectHeader().contentType(TEXT_PLAIN_UTF8)
             .expectBody(String.class).isEqualTo("Hello World!");
  }
}
