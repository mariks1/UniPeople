package com.khasanshin.organizationservice.application;

import com.khasanshin.organizationservice.domain.model.Position;
import com.khasanshin.organizationservice.domain.port.PositionRepositoryPort;
import com.khasanshin.organizationservice.dto.CreatePositionDto;
import com.khasanshin.organizationservice.dto.PositionDto;
import com.khasanshin.organizationservice.dto.UpdatePositionDto;
import com.khasanshin.organizationservice.mapper.PositionMapper;
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
public class PositionApplicationService implements PositionUseCase {

    private final PositionRepositoryPort positionRepository;
    private final PositionMapper positionMapper;

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "name");
    private static final Set<String> ALLOWED_SORT = Set.of("id", "name", "createdAt", "updatedAt");

    @Override
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

    @Override
    public PositionDto get(UUID id) {
        return positionRepository
                .findById(id)
                .map(positionMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("position not found: " + id));
    }

    @Override
    @Transactional
    public PositionDto create(CreatePositionDto dto) {
        if (positionRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new IllegalStateException("position name already exists");
        }
        Position e = positionMapper.toDomain(dto);
        try {
            e = positionRepository.save(e);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("position name already exists", ex);
        }
        return positionMapper.toDto(e);
    }

    @Override
    @Transactional
    public PositionDto update(UUID id, UpdatePositionDto dto) {
        Position e =
                positionRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("position not found: " + id));
        Position updated = positionMapper.updateDomain(dto, e);
        try {
            updated = positionRepository.save(updated);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("position name already exists", ex);
        }
        return positionMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!positionRepository.existsById(id))
            throw new EntityNotFoundException("position not found: " + id);
        positionRepository.deleteById(id);
    }

    @Override
    public boolean exists(UUID id) {
        return positionRepository.existsById(id);
    }
}
