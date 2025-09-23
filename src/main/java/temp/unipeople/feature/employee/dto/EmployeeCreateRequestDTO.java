package temp.unipeople.feature.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Value;

@Value
public class EmployeeCreateRequestDTO {

  @NotBlank String firstName;

  @NotBlank String lastName;

  String middleName;

  @Email @NotBlank String workEmail;

  @Pattern(regexp = "^[+0-9()\\-\\s]{5,}$", message = "invalid phone")
  String phone;
}
