package com.khasanshin.dutyservice.infrastructure.persistence;

import com.khasanshin.dutyservice.domain.model.Duty;
import com.khasanshin.dutyservice.domain.port.DutyRepositoryPort;
import com.khasanshin.dutyservice.repository.DutyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JpaDutyRepositoryAdapter implements DutyRepositoryPort {

    private final DutyRepository dutyRepository;

    @Override
    public Page<Duty> findAll(Pageable pageable) {
        return dutyRepository.findAll(pageable).map(this::toDomain);
    }

    @Override
    public Optional<Duty> findById(UUID id) {
        return dutyRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Duty save(Duty duty) {
        com.khasanshin.dutyservice.entity.Duty saved = dutyRepository.saveAndFlush(toEntity(duty));
        return toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        dutyRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return dutyRepository.existsById(id);
    }

    @Override
    public boolean existsByCodeIgnoreCase(String code) {
        return dutyRepository.existsByCodeIgnoreCase(code);
    }

    private Duty toDomain(com.khasanshin.dutyservice.entity.Duty e) {
        return Duty.builder()
                .id(e.getId())
                .code(e.getCode())
                .name(e.getName())
                .build();
    }

    private com.khasanshin.dutyservice.entity.Duty toEntity(Duty duty) {
        return com.khasanshin.dutyservice.entity.Duty.builder()
                .id(duty.getId())
                .code(duty.getCode())
                .name(duty.getName())
                .build();
    }
}
