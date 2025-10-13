package temp.unipeople.feature.department.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
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

  @NotNull
  @JsonProperty("faculty_id")
  @JsonAlias("faculty_id")
  UUID facultyId;

  @JsonProperty("head_employee_id")
  @JsonAlias("head_employee_id")
  UUID headEmployeeId;
}
