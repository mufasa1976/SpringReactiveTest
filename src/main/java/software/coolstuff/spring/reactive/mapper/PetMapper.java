package software.coolstuff.spring.reactive.mapper;

import reactor.util.function.Tuple2;
import software.coolstuff.spring.reactive.entity.PetEntity;
import software.coolstuff.spring.reactive.model.PetModel;

public interface PetMapper {
  PetModel toModel(PetEntity entity);

  PetEntity toEntity(PetModel model);

  PetEntity populateEntityWithValuesOfResource(Tuple2<PetEntity, PetModel> tuple2Mono);
}
