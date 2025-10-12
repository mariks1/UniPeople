package temp.unipeople.feature.leave.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "leave_request",
    indexes = {
      @Index(name = "idx_leave_request_emp", columnList = "employee_id, date_from"),
      @Index(name = "idx_leave_request_status", columnList = "status")
    })
@EntityListeners(AuditingEntityListener.class)
public class LeaveRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "employee_id", nullable = false)
  private UUID employeeId;

  @Column(name = "type_id", nullable = false)
  private UUID typeId;

  @Column(name = "date_from", nullable = false)
  private LocalDate dateFrom;

  @Column(name = "date_to", nullable = false)
  private LocalDate dateTo;

  public enum Status {
    DRAFT,
    PENDING,
    APPROVED,
    REJECTED,
    CANCELED
  }

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Status status; // DRAFT/PENDING/APPROVED/REJECTED/CANCELED

  @Column(name = "approver_id")
  private UUID approverId;

  @Column(columnDefinition = "text")
  private String comment;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private Instant updatedAt;
}
