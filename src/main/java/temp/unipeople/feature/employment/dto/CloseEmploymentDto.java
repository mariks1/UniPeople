package temp.unipeople.feature.employment.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class CloseEmploymentDto {

  LocalDate endDate;
}
