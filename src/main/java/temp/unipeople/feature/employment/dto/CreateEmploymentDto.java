package temp.unipeople.feature.employment.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class CreateEmploymentDto {

  @NotNull UUID employeeId;

  @NotNull UUID departmentId;

  @NotNull UUID positionId;

  @NotNull LocalDate startDate;

  @DecimalMin(value = "0.01")
  @DecimalMax(value = "2.00")
  BigDecimal rate;

  @PositiveOrZero Integer salary;
}
