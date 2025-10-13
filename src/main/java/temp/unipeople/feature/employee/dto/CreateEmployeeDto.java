package temp.unipeople.feature.employee.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class CreateEmployeeDto {

  @NotBlank
  @Size(max = 100)
  String firstName;

  @NotBlank
  @Size(max = 100)
  @JsonAlias("last_name")
  @JsonProperty("last_name")
  String lastName;

  @Size(max = 100)
  @JsonAlias("middle_name")
  @JsonProperty("middle_name")
  String middleName;

  @Email
  @Size(max = 255)
  @NotBlank
  @JsonProperty("work_email")
  @JsonAlias("work_email")
  String workEmail;

  @Pattern(regexp = "^(?:\\+7|8)\\d{10}$", message = "phone must be 8XXXXXXXXXX or +7XXXXXXXXXX")
  String phone;

  @JsonAlias("department_id")
  @JsonProperty("department_id")
  UUID departmentId;
}
