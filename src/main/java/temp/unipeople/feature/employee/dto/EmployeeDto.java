package temp.unipeople.feature.employee.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeDto {
  UUID id;
  String firstName;
  String lastName;
  String middleName;
  String workEmail;
  String phone;
  UUID departmentId;
  String status;
  Instant createdAt;
  Instant updatedAt;
}
