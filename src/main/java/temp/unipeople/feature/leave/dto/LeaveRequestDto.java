package temp.unipeople.feature.leave.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import temp.unipeople.feature.leave.entity.LeaveRequest;

@Data
@Builder
@Jacksonized
public class LeaveRequestDto {

  UUID id;
  UUID employeeId;
  UUID typeId;
  LocalDate dateFrom;
  LocalDate dateTo;
  LeaveRequest.Status status;
  UUID approverId;
  String comment;
  Instant createdAt;
  Instant updatedAt;
}
