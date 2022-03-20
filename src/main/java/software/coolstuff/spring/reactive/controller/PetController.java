package software.coolstuff.spring.reactive.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import software.coolstuff.spring.reactive.model.PetModel;
import software.coolstuff.spring.reactive.service.PetService;
import software.coolstuff.spring.reactive.utils.ControllerSupport;
import software.coolstuff.spring.reactive.utils.PageableSupport;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequiredArgsConstructor
@RequestMapping(PetController.ENDPOINT)
@Validated
@Slf4j
public class PetController implements ControllerSupport, PageableSupport {
  public static final String ENDPOINT = "api/v1/pets";

  private final PetService petService;

  @GetMapping
  public Mono<ResponseEntity<List<PetModel>>> findAllContainingName(
      @RequestParam("name") Optional<String> name,
      @PageableDefault(size = Integer.MAX_VALUE, sort = "name") Pageable pageable,
      ServerHttpRequest request) {
    return petService.findAll(name.filter(StringUtils::isNotBlank).orElse(null), pageable)
                     .map(page -> pageWithLinkHeaders(request, page));
  }

  @GetMapping("{id}")
  public Mono<ResponseEntity<PetModel>> findOne(@PathVariable("id") long id) {
    return petService.findOne(id)
                     .map(ResponseEntity::ok)
                     .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @PostMapping
  @ResponseStatus(CREATED)
  public Mono<PetModel> create(@RequestBody @Valid Mono<PetModel> pet) {
    return petService.create(pet);
  }

  @PutMapping("{id}")
  public Mono<ResponseEntity<PetModel>> update(@PathVariable("id") long id, @RequestBody @Valid Mono<PetModel> pet) {
    return petService.update(id, pet)
                     .map(ResponseEntity::ok)
                     .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @DeleteMapping("{id}")
  @ResponseStatus(NO_CONTENT)
  public Mono<Void> delete(@PathVariable("id") long id, @RequestHeader(HEADER_VERSION) int version) {
    return petService.delete(id, version);
  }
}
