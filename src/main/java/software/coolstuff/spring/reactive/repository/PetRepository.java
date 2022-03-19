package software.coolstuff.spring.reactive.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.coolstuff.spring.reactive.entity.PetEntity;

public interface PetRepository extends ReactiveSortingRepository<PetEntity, Long> {
  Flux<PetEntity> findAllBy(Pageable pageable);

  Mono<Long> countAllByNameIsContainingIgnoringCase(String name);

  Flux<PetEntity> findAllByNameIsContainingIgnoringCase(String name, Pageable pageable);

  Mono<PetEntity> findByIdAndVersion(long id, int version);
}
