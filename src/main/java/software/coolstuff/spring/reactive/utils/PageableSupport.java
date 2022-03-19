package software.coolstuff.spring.reactive.utils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.*;

public interface PageableSupport {
  String SORT_DELIMITER = " ";

  String SORT_ORDER_ASC = "asc";
  String SORT_ORDER_DESC = "desc";

  String HEADER_TOTAL_ELEMENTS = "X-Total-Elements";

  String QUERY_PARAM_PAGE = "page";
  String QUERY_PARAM_SIZE = "size";
  String QUERY_PARAM_SORT = "sort";

  String DEFAULT_PAGE = "0";
  String DEFAULT_SIZE = "20";

  String LINK_FIRST = "first";
  String LINK_PREV = "prev";
  String LINK_NEXT = "next";
  String LINK_LAST = "last";

  class IllegalSortOrderException extends RuntimeException {
    public IllegalSortOrderException(String property, String invalidSortOrder) {
      super(String.format("Invalid Order '%s' on Property '%s'. Valid Orders are: %s", invalidSortOrder, property, List.of(SORT_ORDER_ASC, SORT_ORDER_DESC)));
    }
  }

  default Pageable toPageable(int page, int size, List<String> sorts) {
    final List<Sort.Order> orders =
        Optional.ofNullable(sorts)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(sort -> {
                  if (isBlank(sort)) {
                    return null;
                  }

                  final String sortOrder = substringAfterLast(sort, SORT_DELIMITER);
                  switch (lowerCase(sortOrder)) {
                    case SORT_ORDER_DESC:
                      return Sort.Order.desc(substringBeforeLast(sort, SORT_DELIMITER));
                    case SORT_ORDER_ASC:
                      return Sort.Order.asc(substringBeforeLast(sort, SORT_DELIMITER));
                    default:
                      return Sort.Order.asc(sort);
                  }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    return PageRequest.of(page, size, Sort.by(orders));
  }

  default <T> ResponseEntity<List<T>> pageWithLinkHeaders(final ServerHttpRequest serverHttpRequest, final Page<T> page) {
    final List<String[]> links = new ArrayList<>();
    links.add(new String[] {ControllerSupport.LINK_SELF, Integer.toString(page.getNumber())});
    if (page.getNumber() > 0) {
      links.add(new String[] {LINK_PREV, Integer.toString(page.getNumber() - 1)});
      links.add(new String[] {LINK_FIRST, "0"});
    }
    if (page.getNumber() < page.getTotalPages() - 1) {
      links.add(new String[] {LINK_NEXT, Integer.toString(page.getNumber() + 1)});
      links.add(new String[] {LINK_LAST, Integer.toString(page.getTotalPages() - 1)});
    }

    final ResponseEntity.BodyBuilder responseEntityBuilder = ResponseEntity.ok()
                                                                           .header(HEADER_TOTAL_ELEMENTS, Long.toString(page.getTotalElements()));
    links.forEach(link -> {

      final String target = UriComponentsBuilder.fromHttpRequest(serverHttpRequest)
                                                .replaceQueryParam(QUERY_PARAM_PAGE, link[1])
                                                .replaceQueryParam(QUERY_PARAM_SIZE, page.getSize())
                                                .toUriString();
      responseEntityBuilder.header(ControllerSupport.HEADER_LINK, String.format("<%s>; rel=\"%s\"; page=\"%s\"; size=\"%s\"", target, link[0], link[1], page.getSize()));
    });
    return responseEntityBuilder.body(page.get().collect(Collectors.toList()));
  }
}
