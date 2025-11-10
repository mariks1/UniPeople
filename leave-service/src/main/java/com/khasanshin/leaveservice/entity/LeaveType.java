package com.khasanshin.leaveservice.entity;

import java.util.UUID;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "leave_type")
public class LeaveType {

  @Id
  private UUID id;

  @Column("code")
  private String code;

  @Column("name")
  private String name;

  @Column("paid")
  private Boolean paid;

  @Column("max_days_per_year")
  private Integer maxDaysPerYear;
}
