package temp.unipeople.feature.leave.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class CreateLeaveRequestDto {

  @NotNull
  @JsonProperty("employee_id")
  @JsonAlias("employee_id")
  UUID employeeId;

  @NotNull
  @JsonProperty("type_id")
  @JsonAlias("type_id")
  UUID typeId;

  @NotNull
  @JsonProperty("date_from")
  @JsonAlias("date_from")
  LocalDate dateFrom;

  @NotNull
  @JsonProperty("date_to")
  @JsonAlias("date_to")
  LocalDate dateTo;

  @Size(max = 1000)
  String comment;

  boolean submit;
}
