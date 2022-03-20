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
import org.springframework.web.util.UriComponentsBuilder;
import software.coolstuff.spring.reactive.config.WebfluxConfiguration;
import software.coolstuff.spring.reactive.controller.HelloWorldController;

import static org.assertj.core.api.Assertions.assertThat;

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

  @Test
  @DisplayName("GET /" + HelloWorldController.ENDPOINT + "/repeat?waitSeconds=0 and take 3 Events and expect 3 times 'Hello World!'")
  void helloWorldRepeated() {
    final var response =
        webClient.get().uri(UriComponentsBuilder.fromPath("/" + HelloWorldController.ENDPOINT)
                                                .query("repeat")
                                                .queryParam("waitSeconds", "0")
                                                .toUriString())
                 .exchange()
                 .expectStatus().isOk()
                 .returnResult(String.class)
                 .getResponseBody()
                 .take(3)
                 .collectList()
                 .block();
    assertThat(response)
        .hasSize(3)
        .containsOnly("Hello World!");
  }
}
