package software.coolstuff.spring.reactive.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import software.coolstuff.spring.reactive.entity.PetEntity;
import software.coolstuff.spring.reactive.mapper.PetMapper;
import software.coolstuff.spring.reactive.model.PetModel;
import software.coolstuff.spring.reactive.repository.PetRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PetServiceImpl implements PetService {
  private final PetRepository petRepository;
  private final PetMapper petMapper;

  @Override
  public Mono<Page<PetModel>> findAll(String name, Pageable pageable) {
    if (StringUtils.isBlank(name)) {
      return petRepository.count()
                          .zipWith(monoFromPets(petRepository.findAllBy(pageable)))
                          .map(toPage(pageable));
    }
    return petRepository.countAllByNameIsContainingIgnoringCase(name)
                        .zipWith(monoFromPets(petRepository.findAllByNameIsContainingIgnoringCase(name, pageable)))
                        .map(toPage(pageable));
  }

  private Mono<List<PetModel>> monoFromPets(Flux<PetEntity> petEntityFlux) {
    return petEntityFlux.map(petMapper::toModel)
                        .buffer()
                        .collect(Collectors.reducing(new ArrayList<>(), (result, list) -> {
                          result.addAll(list);
                          return result;
                        }));
  }

  private <T> Function<Tuple2<Long, List<T>>, Page<T>> toPage(Pageable pageable) {
    return tuple2 -> new PageImpl<>(tuple2.getT2(), pageable, tuple2.getT1());
  }

  @Override
  public Mono<PetModel> findOne(long id) {
    return petRepository.findById(id)
                        .map(petMapper::toModel);
  }

  @Override
  @Transactional
  public Mono<PetModel> create(Mono<PetModel> pet) {
    return pet.map(petMapper::toEntity)
              .flatMap(petRepository::save)
              .map(petMapper::toModel);
  }

  @Override
  @Transactional
  public Mono<PetModel> update(long id, Mono<PetModel> pet) {
    return petRepository.findById(id)
                        .zipWith(pet)
                        .map(petMapper::populateEntityWithValuesOfResource)
                        .flatMap(petRepository::save)
                        .map(petMapper::toModel);
  }

  @Override
  @Transactional
  public Mono<Void> delete(long id, int version) {
    return petRepository.findByIdAndVersion(id, version)
                        .flatMap(petRepository::delete);
  }
}
