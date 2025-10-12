package temp.unipeople.feature.leave.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;

@Data
public class UpdateLeaveRequestDto {

  LocalDate dateFrom;
  LocalDate dateTo;

  @Size(max = 1000)
  String comment;
}
