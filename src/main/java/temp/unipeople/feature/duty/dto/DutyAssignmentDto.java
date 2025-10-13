package temp.unipeople.feature.duty.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class DutyAssignmentDto {
  UUID id;
  UUID departmentId;
  UUID employeeId;
  UUID dutyId;
  UUID assignedBy;
  Instant assignedAt;
  String note;
}
