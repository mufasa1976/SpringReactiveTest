package software.coolstuff.spring.reactive.entity;

import com.querydsl.core.annotations.QueryEntity;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;
import software.coolstuff.spring.reactive.model.PetModel;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static lombok.AccessLevel.NONE;

@Data
@Builder
@Table("pets")
@QueryEntity
public class PetEntity {
  public static final String TABLE_NAME = "pets";

  @Id
  @Setter(NONE)
  private Long id;
  @Version
  @Builder.Default
  private int version = 0;
  @NotNull
  private PetModel.Type type;
  @NotEmpty
  @Size(max = 250)
  private String name;
}
