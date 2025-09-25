package temp.unipeople.feature.duty.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
        name = "department_duty_assignment",
        indexes = {
                @Index(name = "idx_duty_assign_dept", columnList = "department_id"),
                @Index(name = "idx_duty_assign_emp", columnList = "employee_id")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true)
@EntityListeners(AuditingEntityListener.class)
public class DepartmentDutyAssignment {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(name = "department_id", nullable = false)
    private UUID departmentId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "duty_id", nullable = false)
    private UUID dutyId;

    @Column(name = "assigned_by")
    private UUID assignedBy;

    @CreatedDate
    @Column(name = "assigned_at", updatable = false)
    private Instant assignedAt;

    @Column(name = "note", length = 255)
    private String note;

    @PrePersist
    void prePersist() {
        if (assignedAt == null) assignedAt = Instant.now();
    }
}
