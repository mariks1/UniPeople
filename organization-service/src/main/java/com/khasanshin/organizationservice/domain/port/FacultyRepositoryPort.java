package com.khasanshin.organizationservice.domain.port;

import com.khasanshin.organizationservice.domain.model.Faculty;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FacultyRepositoryPort {
    boolean existsById(UUID id);

    Optional<Faculty> findById(UUID id);

    Faculty save(Faculty faculty);

    void deleteById(UUID id);

    Page<Faculty> findAll(Pageable pageable);
}
