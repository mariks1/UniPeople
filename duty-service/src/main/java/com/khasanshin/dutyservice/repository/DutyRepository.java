package com.khasanshin.dutyservice.repository;

import java.util.UUID;

import com.khasanshin.dutyservice.entity.Duty;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DutyRepository extends JpaRepository<Duty, UUID> {
  boolean existsByCodeIgnoreCase(String code);
}
