package temp.unipeople.feature.department.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class UpdateDepartmentDto {
  String code;
  String name;

  @JsonAlias("faculty_id")
  @JsonProperty("faculty_id")
  UUID facultyId;

  @JsonProperty("head_employee_id")
  @JsonAlias("head_employee_id")
  UUID headEmployeeId;
}
