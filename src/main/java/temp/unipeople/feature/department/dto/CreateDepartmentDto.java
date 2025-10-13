package temp.unipeople.feature.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class CreateDepartmentDto {

  @NotBlank String code;
  @NotBlank String name;
  @NotNull UUID facultyId;
  UUID headEmployeeId;
}
