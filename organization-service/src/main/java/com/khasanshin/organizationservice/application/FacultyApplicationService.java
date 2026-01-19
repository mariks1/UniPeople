package com.khasanshin.organizationservice.application;

import com.khasanshin.organizationservice.domain.model.Faculty;
import com.khasanshin.organizationservice.domain.port.FacultyRepositoryPort;
import com.khasanshin.organizationservice.dto.CreateFacultyDto;
import com.khasanshin.organizationservice.dto.FacultyDto;
import com.khasanshin.organizationservice.dto.UpdateFacultyDto;
import com.khasanshin.organizationservice.mapper.FacultyMapper;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FacultyApplicationService implements FacultyUseCase {

  private final FacultyRepositoryPort repo;
  private final FacultyMapper mapper;

  @Override
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

  @Override
  public FacultyDto get(UUID id) {
    Faculty d =
        repo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("employee not found: " + id));
    return mapper.toDto(d);
  }

  @Override
  @Transactional
  public FacultyDto create(CreateFacultyDto dto) {
    Faculty faculty = mapper.toDomain(dto);
    return mapper.toDto(repo.save(faculty));
  }

  @Override
  @Transactional
  public FacultyDto update(UUID id, UpdateFacultyDto dto) {
    Faculty e =
        repo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("faculty not found: " + id));

    Faculty updated = mapper.updateDomain(dto, e);
    return mapper.toDto(repo.save(updated));
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    if (!repo.existsById(id)) {
      throw new EntityNotFoundException("faculty not found: " + id);
    }
    repo.deleteById(id);
  }
}
