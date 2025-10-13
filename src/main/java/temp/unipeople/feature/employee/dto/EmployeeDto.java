package temp.unipeople.feature.employee.dto;

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
  String firstName;
  String lastName;
  String middleName;
  String workEmail;
  String phone;
  UUID departmentId;
  Employee.Status status;
  Instant createdAt;
  Instant updatedAt;
}
