package com.khasanshin.organizationservice.infrastructure.persistence;

import com.khasanshin.organizationservice.domain.model.Position;
import com.khasanshin.organizationservice.domain.port.PositionRepositoryPort;
import com.khasanshin.organizationservice.repository.PositionRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaPositionRepositoryAdapter implements PositionRepositoryPort {

    private final PositionRepository repository;

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return repository.existsByNameIgnoreCase(name);
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public Optional<Position> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Position save(Position position) {
        return toDomain(repository.saveAndFlush(toEntity(position)));
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public Page<Position> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDomain);
    }

    @Override
    public Page<Position> findByNameContainingIgnoreCase(String q, Pageable pageable) {
        return repository.findByNameContainingIgnoreCase(q, pageable).map(this::toDomain);
    }

    private Position toDomain(com.khasanshin.organizationservice.entity.Position e) {
        return Position.builder()
                .id(e.getId())
                .name(e.getName())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private com.khasanshin.organizationservice.entity.Position toEntity(Position p) {
        return com.khasanshin.organizationservice.entity.Position.builder()
                .id(p.getId())
                .name(p.getName())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
