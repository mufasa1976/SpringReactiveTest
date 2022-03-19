package software.coolstuff.spring.reactive.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class PetModel {
  public enum Type {
    CAT, DOG
  }

  private Long id;
  private Integer version;
  @NotNull
  private Type type;
  @NotBlank
  @Size(max = 250)
  private String name;

  @Builder
  @JsonCreator
  public PetModel(
      @JsonProperty("id") Long id,
      @JsonProperty("version") Integer version,
      @JsonProperty("type") Type type,
      @JsonProperty("name") String name) {
    this.id = id;
    this.version = version;
    this.type = type;
    this.name = name;
  }
}
