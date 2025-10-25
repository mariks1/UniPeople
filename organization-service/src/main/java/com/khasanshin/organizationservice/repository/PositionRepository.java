package com.khasanshin.organizationservice.repository;

import java.util.UUID;

import com.khasanshin.organizationservice.entity.Position;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, UUID> {

  boolean existsByNameIgnoreCase(String name);

  Page<Position> findByNameContainingIgnoreCase(
      String q, Pageable pageable);
}
