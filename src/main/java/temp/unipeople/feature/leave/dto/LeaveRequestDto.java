package temp.unipeople.feature.leave.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;
import temp.unipeople.feature.leave.entity.LeaveRequest;

@Data
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
