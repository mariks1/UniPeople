package com.khasanshin.organizationservice.service;

import com.khasanshin.organizationservice.dto.CreateFacultyDto;
import com.khasanshin.organizationservice.dto.FacultyDto;
import com.khasanshin.organizationservice.dto.UpdateFacultyDto;
import com.khasanshin.organizationservice.entity.Faculty;
import com.khasanshin.organizationservice.mapper.FacultyMapper;
import com.khasanshin.organizationservice.repository.FacultyRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FacultyService {

  private final FacultyRepository repo;
  private final FacultyMapper mapper;

  public Page<FacultyDto> page(Pageable pageable) {
    Pageable sorted =
        pageable.getSort().isSorted()
            ? pageable
            : PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Order.asc("code"), Sort.Order.asc("name")));
    return repo.findAll(sorted).map(mapper::toDto);
  }

  public FacultyDto get(UUID id) {
    Faculty d =
        repo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("employee not found: " + id));
    return mapper.toDto(d);
  }

  @Transactional
  public FacultyDto create(CreateFacultyDto dto) {
    Faculty faculty = mapper.toEntity(dto);
    return mapper.toDto(repo.save(faculty));
  }

  @Transactional
  public FacultyDto update(UUID id, UpdateFacultyDto dto) {
    Faculty e =
        repo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("faculty not found: " + id));

    mapper.updateEntity(dto, e);
    return mapper.toDto(e);
  }
}
