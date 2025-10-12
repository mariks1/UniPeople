package temp.unipeople.feature.department.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateDepartmentRequest {
  String code;
  String name;
  UUID facultyId;
  UUID headEmployeeId;
}
