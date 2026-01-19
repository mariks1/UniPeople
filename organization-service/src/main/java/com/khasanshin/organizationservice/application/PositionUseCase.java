package com.khasanshin.organizationservice.application;

import com.khasanshin.organizationservice.dto.CreatePositionDto;
import com.khasanshin.organizationservice.dto.PositionDto;
import com.khasanshin.organizationservice.dto.UpdatePositionDto;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PositionUseCase {

    Page<PositionDto> findAll(String q, Pageable pageable);

    PositionDto get(UUID id);

    PositionDto create(CreatePositionDto dto);

    PositionDto update(UUID id, UpdatePositionDto dto);

    void delete(UUID id);

    boolean exists(UUID id);
}
