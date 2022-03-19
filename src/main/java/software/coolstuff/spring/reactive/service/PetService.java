package software.coolstuff.spring.reactive.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;
import software.coolstuff.spring.reactive.model.PetModel;

public interface PetService {
  Mono<Page<PetModel>> findAll(String name, Pageable pageable);

  Mono<PetModel> findOne(long id);

  Mono<PetModel> create(Mono<PetModel> pet);

  Mono<PetModel> update(long id, Mono<PetModel> pet);

  Mono<Void> delete(long id, int version);
}
