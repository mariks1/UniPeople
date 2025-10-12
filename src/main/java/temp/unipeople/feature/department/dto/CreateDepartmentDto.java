package temp.unipeople.feature.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class CreateDepartmentDto {

  @NotBlank String code;
  @NotBlank String name;
  @NotNull UUID facultyId;
  UUID headEmployeeId;
}
