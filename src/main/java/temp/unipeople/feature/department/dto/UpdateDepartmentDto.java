package temp.unipeople.feature.department.dto;

import java.util.UUID;
import lombok.Data;

@Data
public class UpdateDepartmentDto {
  String code;
  String name;
  UUID facultyId;
  UUID headEmployeeId;
}
