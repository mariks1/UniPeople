package temp.unipeople.feature.duty.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class UpdateDutyDto {

  @Pattern(regexp = "^(?=.*\\S).+$", message = "code must contain a non-space char")
  @Size(max = 64)
  String code;

  @Pattern(regexp = "^(?=.*\\S).+$", message = "code must contain a non-space char")
  @Size(max = 150)
  String name;
}
