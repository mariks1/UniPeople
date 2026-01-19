package com.khasanshin.organizationservice.domain.port;

import com.khasanshin.organizationservice.domain.model.Position;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PositionRepositoryPort {

    boolean existsByNameIgnoreCase(String name);

    boolean existsById(UUID id);

    Optional<Position> findById(UUID id);

    Position save(Position position);

    void deleteById(UUID id);

    Page<Position> findAll(Pageable pageable);

    Page<Position> findByNameContainingIgnoreCase(String q, Pageable pageable);
}
