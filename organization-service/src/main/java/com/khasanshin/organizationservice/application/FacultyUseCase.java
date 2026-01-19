package com.khasanshin.organizationservice.application;

import com.khasanshin.organizationservice.dto.CreateFacultyDto;
import com.khasanshin.organizationservice.dto.FacultyDto;
import com.khasanshin.organizationservice.dto.UpdateFacultyDto;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FacultyUseCase {

    Page<FacultyDto> page(Pageable pageable);

    FacultyDto get(UUID id);

    FacultyDto create(CreateFacultyDto dto);

    FacultyDto update(UUID id, UpdateFacultyDto dto);

    void delete(UUID id);
}
