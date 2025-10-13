package temp.unipeople.feature.department.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class DepartmentDto {

  UUID id;

  String code;

  String name;

  @JsonProperty("faculty_id")
  @JsonAlias("faculty_id")
  UUID facultyId;

  @JsonProperty("head_employee_id")
  @JsonAlias("head_employee_id")
  UUID headEmployeeId;

  @JsonProperty("created_at")
  @JsonAlias("created_at")
  Instant createdAt;

  @JsonProperty("updated_at")
  @JsonAlias("updated_at")
  Instant updatedAt;
}
