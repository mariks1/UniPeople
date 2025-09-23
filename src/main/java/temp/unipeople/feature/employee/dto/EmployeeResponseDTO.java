package temp.unipeople.feature.employee.dto;

import java.util.UUID;
import lombok.Value;

@Value
public class EmployeeResponseDTO {
  UUID id;
  String firstName;
  String lastName;
  String middleName;
  String workEmail;
  String phone;
  String status;
}
