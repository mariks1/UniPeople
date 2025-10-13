package temp.unipeople.feature.leave.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class LeaveTypeDto {

  private UUID id;
  private String name;
  private String code;
  boolean paid;

  @JsonProperty("max_days_per_year")
  @JsonAlias("max_days_per_year")
  Integer maxDaysPerYear;
}
