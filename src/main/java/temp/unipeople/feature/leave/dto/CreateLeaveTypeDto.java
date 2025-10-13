package temp.unipeople.feature.leave.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
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
