package temp.unipeople.feature.employee.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import temp.unipeople.feature.employee.entity.Employee;

@Data
@Builder
@Jacksonized
public class EmployeeDto {
  UUID id;

  @JsonAlias("first_name")
  @JsonProperty("first_name")
  String firstName;

  @JsonAlias("last_name")
  @JsonProperty("last_name")
  String lastName;

  @JsonAlias("middle_name")
  @JsonProperty("middle_name")
  String middleName;

  @JsonAlias("work_email")
  @JsonProperty("work_email")
  String workEmail;

  String phone;

  @JsonAlias("department_id")
  @JsonProperty("department_id")
  UUID departmentId;

  Employee.Status status;

  @JsonAlias("created_at")
  @JsonProperty("created_at")
  Instant createdAt;

  @JsonAlias("updated_at")
  @JsonProperty("updated_at")
  Instant updatedAt;
}
