package temp.unipeople.feature.employment.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;
import temp.unipeople.feature.employment.entity.Employment;

@Data
public class EmploymentDto {
  UUID id;
  UUID employeeId;
  UUID departmentId;
  UUID positionId;
  LocalDate startDate;
  LocalDate endDate;
  BigDecimal rate;
  Integer salary;
  Employment.Status status;
  Instant createdAt;
  Instant updatedAt;
}
