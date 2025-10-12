package temp.unipeople.feature.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateEmployeeDto {

  @NotBlank
  @Size(max = 100)
  String firstName;

  @NotBlank
  @Size(max = 100)
  String lastName;

  @Size(max = 100)
  String middleName;

  @Email
  @Size(max = 255)
  @NotBlank
  String workEmail;

  @Pattern(regexp = "^(?:\\+7|8)\\d{10}$", message = "phone must be 8XXXXXXXXXX or +7XXXXXXXXXX")
  String phone;

  UUID departmentId;
}
