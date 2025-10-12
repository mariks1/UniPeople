package temp.unipeople.feature.position.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePositionDto {

  @NotBlank
  @Size(max = 150)
  String name;
}
