package temp.unipeople.feature.department.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class UpdateDepartmentDto {
  String code;
  String name;
  UUID facultyId;
  UUID headEmployeeId;
}
