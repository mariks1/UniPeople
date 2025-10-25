package com.khasanshin.leaveservice.entity;

import java.time.*;
import java.util.UUID;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "leave_request")
public class LeaveRequest {

  @Id
  private UUID id;

  @Column("employee_id")
  private UUID employeeId;

  @Column("type_id")
  private UUID typeId;

  @Column("date_from")
  private LocalDate dateFrom;

  @Column("date_to")
  private LocalDate dateTo;

  public enum Status {
    DRAFT,
    PENDING,
    APPROVED,
    REJECTED,
    CANCELED
  }

  @Column("status")
  private Status status; // DRAFT/PENDING/APPROVED/REJECTED/CANCELED

  @Column("approver_id")
  private UUID approverId;

  @Column("comment")
  private String comment;

  @CreatedDate
  @Column("created_at")
  private Instant createdAt;

  @LastModifiedDate
  @Column("updated_at")
  private Instant updatedAt;
}
