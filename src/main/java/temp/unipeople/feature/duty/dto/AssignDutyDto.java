package temp.unipeople.feature.duty.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class AssignDutyDto {

  @NotNull
  @JsonAlias("employee_id")
  @JsonProperty("employee_id")
  UUID employeeId;

  @NotNull
  @JsonAlias("duty_id")
  @JsonProperty("duty_id")
  UUID dutyId;

  @Size(max = 255)
  String note;

  @JsonAlias("assigned_by")
  @JsonProperty("assigned_by")
  UUID assignedBy;
}
