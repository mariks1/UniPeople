package temp.unipeople.feature.duty.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssignDutyDto {

  @NotNull UUID employeeId;

  @NotNull UUID dutyId;

  @Size(max = 255)
  String note;

  UUID assignedBy;
}
