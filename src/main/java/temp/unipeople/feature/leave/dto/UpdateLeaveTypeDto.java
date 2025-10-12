package temp.unipeople.feature.leave.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateLeaveTypeDto {

  @Pattern(regexp = "^(?=.*\\S).+$")
  @Size(max = 64)
  String code;

  @Pattern(regexp = "^(?=.*\\S).+$")
  @Size(max = 150)
  String name;

  Boolean paid;

  @Positive Integer maxDaysPerYear;
}
