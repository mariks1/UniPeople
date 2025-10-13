package temp.unipeople.feature.department.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class DepartmentDto {

  UUID id;

  String code;

  String name;

  UUID facultyId;

  UUID headEmployeeId;

  Instant createdAt;

  Instant updatedAt;
}
