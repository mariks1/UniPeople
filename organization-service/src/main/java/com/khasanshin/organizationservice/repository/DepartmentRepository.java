package com.khasanshin.organizationservice.repository;

import java.util.UUID;

import com.khasanshin.organizationservice.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {
  @Modifying
  @Query("update Department d set d.headEmployee = null where d.headEmployee = :employeeId")
  void clearHeadByEmployeeId(@Param("employeeId") UUID employeeId);
}
