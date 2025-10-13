package temp.unipeople.feature.faculty.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class CreateFacultyDto {

  @NotNull
  @Size(max = 255)
  String name;

  @NotNull
  @Size(max = 64)
  String code;
}
