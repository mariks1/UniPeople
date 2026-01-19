package com.khasanshin.organizationservice.infrastructure.persistence;

import com.khasanshin.organizationservice.domain.model.Faculty;
import com.khasanshin.organizationservice.domain.port.FacultyRepositoryPort;
import com.khasanshin.organizationservice.repository.FacultyRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaFacultyRepositoryAdapter implements FacultyRepositoryPort {

    private final FacultyRepository repository;

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public Optional<Faculty> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Faculty save(Faculty faculty) {
        return toDomain(repository.save(faculty(faculty)));
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public Page<Faculty> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDomain);
    }

    private Faculty toDomain(com.khasanshin.organizationservice.entity.Faculty e) {
        return Faculty.builder()
                .id(e.getId())
                .code(e.getCode())
                .name(e.getName())
                .build();
    }

    private com.khasanshin.organizationservice.entity.Faculty faculty(Faculty f) {
        return com.khasanshin.organizationservice.entity.Faculty.builder()
                .id(f.getId())
                .code(f.getCode())
                .name(f.getName())
                .build();
    }
}
