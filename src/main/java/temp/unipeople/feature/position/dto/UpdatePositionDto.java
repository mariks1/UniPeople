package temp.unipeople.feature.position.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePositionDto {

  @Pattern(regexp = "^(?=.*\\S).+$", message = "name must contain a non-whitespace character")
  @Size(max = 150)
  String name;
}
