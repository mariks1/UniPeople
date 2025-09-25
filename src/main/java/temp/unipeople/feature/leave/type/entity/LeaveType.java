package temp.unipeople.feature.leave.type.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "leave_type")
public class LeaveType {

  @Id private UUID id;

  @Column(nullable = false, unique = true)
  private String code;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private Boolean paid;

  @Column(name = "max_days_per_year")
  private Integer maxDaysPerYear;
}
