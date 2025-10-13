package temp.unipeople.feature.leave.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
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

  @JsonAlias("employee_id")
  @JsonProperty("employee_id")
  UUID employeeId;

  @JsonAlias("type_id")
  @JsonProperty("type_id")
  UUID typeId;

  @JsonAlias("date_from")
  @JsonProperty("date_from")
  LocalDate dateFrom;

  @JsonAlias("date_to")
  @JsonProperty("date_to")
  LocalDate dateTo;

  LeaveRequest.Status status;

  @JsonAlias("approver_id")
  @JsonProperty("approver_id")
  UUID approverId;

  String comment;

  @JsonAlias("created_at")
  @JsonProperty("created_at")
  Instant createdAt;

  @JsonAlias("updated_at")
  @JsonProperty("updated_at")
  Instant updatedAt;
}
