package software.coolstuff.spring.reactive.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import reactor.util.function.Tuple2;
import software.coolstuff.spring.reactive.entity.PetEntity;
import software.coolstuff.spring.reactive.model.PetModel;

@Mapper(implementationName = "PetMapperImpl")
public abstract class AbstractPetMapperImpl implements PetMapper {
  @Override
  public PetEntity populateEntityWithValuesOfResource(Tuple2<PetEntity, PetModel> tuple2Mono) {
    final var entity = tuple2Mono.getT1();
    populateEntityWithValuesOfResource(entity, tuple2Mono.getT2());
    return entity;
  }

  protected abstract void populateEntityWithValuesOfResource(@MappingTarget PetEntity entity, PetModel model);
}
