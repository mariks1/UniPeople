package temp.unipeople.feature.duty.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DutyAssignmentDto {
  UUID id;
  UUID departmentId;
  UUID employeeId;
  UUID dutyId;
  UUID assignedBy;
  Instant assignedAt;
  String note;
}
