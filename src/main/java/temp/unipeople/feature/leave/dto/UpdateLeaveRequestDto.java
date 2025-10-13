package temp.unipeople.feature.leave.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class UpdateLeaveRequestDto {

  LocalDate dateFrom;
  LocalDate dateTo;

  @Size(max = 1000)
  String comment;
}
