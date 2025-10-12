package temp.unipeople.feature.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateDepartmentRequest {

  @NotBlank String code;
  @NotBlank String name;
  @NotNull UUID facultyId;
  UUID headEmployeeId;
}
