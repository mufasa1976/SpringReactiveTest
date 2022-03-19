package software.coolstuff.spring.reactive.utils;

import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.function.Function;

public interface ControllerSupport {
  String HEADER_VERSION = "X-Version";

  String HEADER_LINK = "Link";

  String LINK_SELF = "self";

  default <T> Function<T, ResponseEntity<T>> okWithLinkHeader(final ServerHttpRequest serverHttpRequest) {
    return entity -> {
      final String target = UriComponentsBuilder.fromHttpRequest(serverHttpRequest).toUriString();
      return ResponseEntity.ok()
                           .header(HEADER_LINK, String.format("<%s>; rel=\"%s\"", target, LINK_SELF))
                           .body(entity);
    };
  }
}
