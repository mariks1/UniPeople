package com.khasanshin.dutyservice.application;

import com.khasanshin.dutyservice.domain.model.Duty;
import com.khasanshin.dutyservice.domain.port.DutyAssignmentRepositoryPort;
import com.khasanshin.dutyservice.domain.port.DutyRepositoryPort;
import com.khasanshin.dutyservice.dto.CreateDutyDto;
import com.khasanshin.dutyservice.dto.DutyAssignmentDto;
import com.khasanshin.dutyservice.dto.DutyDto;
import com.khasanshin.dutyservice.dto.UpdateDutyDto;
import com.khasanshin.dutyservice.mapper.DutyAssignmentMapper;
import com.khasanshin.dutyservice.mapper.DutyMapper;
import jakarta.persistence.EntityNotFoundException;
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
public class DutyApplicationService implements DutyUseCase {

    private final DutyRepositoryPort dutyRepository;
    private final DutyAssignmentRepositoryPort dutyAssignmentRepository;
    private final DutyMapper dutyMapper;
    private final DutyAssignmentMapper dutyAssignmentMapper;

    @Override
    public Page<DutyDto> findAll(Pageable pageable) {
        Pageable sorted =
                pageable.getSort().isSorted()
                        ? pageable
                        : PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.ASC, "code"));
        return dutyRepository.findAll(sorted).map(dutyMapper::toDto);
    }

    @Override
    public DutyDto get(UUID id) {
        Duty e =
                dutyRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("duty not found: " + id));
        return dutyMapper.toDto(e);
    }

    @Override
    @Transactional
    public DutyDto create(CreateDutyDto dto) {
        if (dutyRepository.existsByCodeIgnoreCase(dto.getCode())) {
            throw new IllegalStateException("duty code already exists");
        }
        Duty e = dutyMapper.toDomain(dto);
        try {
            e = dutyRepository.save(e);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("duty code already exists", ex);
        }
        return dutyMapper.toDto(e);
    }

    @Override
    @Transactional
    public DutyDto update(UUID id, UpdateDutyDto dto) {
        Duty e =
                dutyRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("duty not found: " + id));
        e = dutyMapper.updateDomain(dto, e);
        try {
            e = dutyRepository.save(e);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("duty code already exists", ex);
        }
        return dutyMapper.toDto(e);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!dutyRepository.existsById(id)) {
            throw new EntityNotFoundException("duty not found: " + id);
        }
        dutyRepository.deleteById(id);
    }

    @Override
    public Page<DutyAssignmentDto> listAssignments(UUID dutyId, Pageable pageable) {
        Pageable sorted =
                pageable.getSort().isSorted()
                        ? pageable
                        : PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "assignedAt"));
        return dutyAssignmentRepository.findByDutyId(dutyId, sorted).map(dutyAssignmentMapper::toDto);
    }
}
