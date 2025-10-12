package temp.unipeople.feature.leave.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateLeaveTypeDto {

  @NotBlank
  @Size(max = 64)
  private String code;

  @NotBlank
  @Size(max = 150)
  private String name;

  boolean paid;

  @Positive Integer maxDaysPerYear;
}
