package temp.unipeople.feature.duty.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class CreateDutyDto {

  @NotBlank
  @Size(max = 64)
  private String code;

  @NotBlank
  @Size(max = 150)
  private String name;
}
