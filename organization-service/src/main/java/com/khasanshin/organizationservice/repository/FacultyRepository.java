package com.khasanshin.organizationservice.repository;

import java.util.UUID;

import com.khasanshin.organizationservice.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacultyRepository extends JpaRepository<Faculty, UUID> {}
