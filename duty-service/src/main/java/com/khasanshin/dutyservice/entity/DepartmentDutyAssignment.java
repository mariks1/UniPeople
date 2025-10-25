package com.khasanshin.dutyservice.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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
public class DepartmentDutyAssignment {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "department_id", nullable = false)
  private UUID departmentId;

  @Column(name = "employee_id", nullable = false)
  private UUID employeeId;

  @Column(name = "duty_id", nullable = false)
  private UUID dutyId;

  @Column(name = "assigned_by")
  private UUID assignedBy;

  @CreationTimestamp
  @Column(name = "assigned_at", updatable = false, nullable = false)
  private Instant assignedAt;

  @Column(name = "note", length = 255)
  private String note;
}
