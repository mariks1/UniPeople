package temp.unipeople.feature.leave.request.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import java.time.*;
import java.util.UUID;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Table(name="leave_request")
public class LeaveRequest {
    @Id private UUID id;

    @Column(name="employee_id", nullable=false)
    private UUID employeeId;

    @Column(name="type_id", nullable=false)
    private UUID typeId;

    @Column(name="date_from", nullable=false)
    private LocalDate dateFrom;

    @Column(name="date_to", nullable=false)
    private LocalDate dateTo;

    @Column(nullable=false)
    private String status; // DRAFT/PENDING/APPROVED/REJECTED/CANCELED

    @Column(name="approver_id")
    private UUID approverId;

    @Column(columnDefinition="text")
    private String comment;

    @CreatedDate
    @Column(name="created_at", updatable=false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name="updated_at")
    private Instant updatedAt;
}
