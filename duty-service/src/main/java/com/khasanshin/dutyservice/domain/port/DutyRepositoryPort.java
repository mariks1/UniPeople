package com.khasanshin.dutyservice.domain.port;

import com.khasanshin.dutyservice.domain.model.Duty;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DutyRepositoryPort {

    Page<Duty> findAll(Pageable pageable);

    Optional<Duty> findById(UUID id);

    Duty save(Duty duty);

    void deleteById(UUID id);

    boolean existsById(UUID id);

    boolean existsByCodeIgnoreCase(String code);
}
