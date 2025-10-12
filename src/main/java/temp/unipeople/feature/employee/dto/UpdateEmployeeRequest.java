package temp.unipeople.feature.employee.dto;

import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateEmployeeRequest {

  @Size(max = 100)
  String firstName;

  @Size(max = 100)
  String lastName;

  @Size(max = 100)
  String middleName;

  UUID departmentId;
}
