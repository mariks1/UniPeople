package com.khasanshin.organizationservice.service;

import com.khasanshin.organizationservice.dto.CreatePositionDto;
import com.khasanshin.organizationservice.dto.PositionDto;
import com.khasanshin.organizationservice.dto.UpdatePositionDto;
import com.khasanshin.organizationservice.entity.Position;
import com.khasanshin.organizationservice.mapper.PositionMapper;
import com.khasanshin.organizationservice.repository.PositionRepository;
import jakarta.persistence.EntityNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PositionService {

  private final PositionRepository positionRepository;
  private final PositionMapper positionMapper;

  private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "name");
  private static final Set<String> ALLOWED_SORT =
          Set.of("id", "name", "createdAt", "updatedAt");

  @Transactional(readOnly = true)
  public Page<PositionDto> findAll(String q, Pageable pageable) {
    Sort sort = sanitizeSort(pageable.getSort());
    Pageable p = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

    Page<Position> page = (q == null || q.isBlank())
            ? positionRepository.findAll(p)
            : positionRepository.findByNameContainingIgnoreCase(q.trim(), p);

    return page.map(positionMapper::toDto);
  }

  private Sort sanitizeSort(Sort incoming) {
    List<Sort.Order> safe = new ArrayList<>();
    for (Sort.Order o : incoming) {
      if (ALLOWED_SORT.contains(o.getProperty())) {
        safe.add(o);
      }
    }
    return safe.isEmpty() ? DEFAULT_SORT : Sort.by(safe);
  }

  public PositionDto get(UUID id) {
    return positionRepository
        .findById(id)
        .map(positionMapper::toDto)
        .orElseThrow(() -> new EntityNotFoundException("position not found: " + id));
  }

  @Transactional
  public PositionDto create(CreatePositionDto dto) {
    if (positionRepository.existsByNameIgnoreCase(dto.getName())) {
      throw new IllegalStateException("position name already exists");
    }
    Position e = positionMapper.toEntity(dto);
    try {
      e = positionRepository.saveAndFlush(e);
    } catch (DataIntegrityViolationException ex) {
      throw new IllegalStateException("position name already exists", ex);
    }
    return positionMapper.toDto(e);
  }

  @Transactional
  public PositionDto update(UUID id, UpdatePositionDto dto) {
    Position e =
        positionRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("position not found: " + id));
    positionMapper.updateEntity(dto, e);
    try {
      positionRepository.flush();
    } catch (DataIntegrityViolationException ex) {
      throw new IllegalStateException("position name already exists", ex);
    }
    return positionMapper.toDto(e);
  }

  @Transactional
  public void delete(UUID id) {
    if (!positionRepository.existsById(id))
      throw new EntityNotFoundException("position not found: " + id);
    positionRepository.deleteById(id);
  }

  public boolean exists(UUID id) {
    return positionRepository.existsById(id);
  }

}
