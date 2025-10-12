package temp.unipeople.feature.faculty.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateFacultyRequest {

  @NotNull
  @Size(max = 255)
  String name;

  @NotNull
  @Size(max = 64)
  String code;
}
