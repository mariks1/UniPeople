package temp.unipeople.feature.department.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateDepartmentDto {
  String code;
  String name;
  UUID facultyId;
  UUID headEmployeeId;
}
