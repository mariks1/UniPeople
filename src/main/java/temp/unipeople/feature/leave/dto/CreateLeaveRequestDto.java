package temp.unipeople.feature.leave.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class CreateLeaveRequestDto {

  @NotNull UUID employeeId;
  @NotNull UUID typeId;

  @NotNull LocalDate dateFrom;
  @NotNull LocalDate dateTo;

  @Size(max = 1000)
  String comment;

  boolean submit;
}
