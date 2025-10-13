package temp.unipeople.feature.employee.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class UpdateEmployeeDto {

  @Size(max = 100)
  @JsonAlias("first_name")
  @JsonProperty("first_name")
  String firstName;

  @Size(max = 100)
  @JsonAlias("last_name")
  @JsonProperty("last_name")
  String lastName;

  @Size(max = 100)
  @JsonAlias("middle_name")
  @JsonProperty("middle_name")
  String middleName;

  @JsonAlias("department_id")
  @JsonProperty("department_id")
  UUID departmentId;
}
