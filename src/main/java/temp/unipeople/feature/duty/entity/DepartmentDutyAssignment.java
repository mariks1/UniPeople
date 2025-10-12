package temp.unipeople.feature.duty.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import temp.unipeople.feature.department.entity.Department;
import temp.unipeople.feature.employee.entity.Employee;

@Entity
@Table(
    name = "department_duty_assignment",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_dept_emp_duty",
          columnNames = {"department_id", "employee_id", "duty_id"})
    },
    indexes = {
      @Index(name = "idx_duty_assign_dept", columnList = "department_id"),
      @Index(name = "idx_duty_assign_emp", columnList = "employee_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EntityListeners(AuditingEntityListener.class)
public class DepartmentDutyAssignment {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "department_id", foreignKey = @ForeignKey(name = "fk_duty_assign_department"))
  private Department department;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "employee_id", foreignKey = @ForeignKey(name = "fk_duty_assign_employee"))
  private Employee employee;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "duty_id", foreignKey = @ForeignKey(name = "fk_duty_assign_duty"))
  private Duty duty;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assigned_by", foreignKey = @ForeignKey(name = "fk_duty_assign_by"))
  private Employee assignedBy;

  @CreatedDate
  @Column(name = "assigned_at", updatable = false, nullable = false)
  private Instant assignedAt;

  @Column(name = "note", length = 255)
  private String note;
}
