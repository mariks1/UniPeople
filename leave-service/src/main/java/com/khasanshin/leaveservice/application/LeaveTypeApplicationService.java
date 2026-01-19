package com.khasanshin.leaveservice.application;

import com.khasanshin.leaveservice.domain.model.LeaveType;
import com.khasanshin.leaveservice.domain.port.LeaveTypeRepositoryPort;
import com.khasanshin.leaveservice.dto.CreateLeaveTypeDto;
import com.khasanshin.leaveservice.dto.LeaveTypeDto;
import com.khasanshin.leaveservice.dto.UpdateLeaveTypeDto;
import com.khasanshin.leaveservice.mapper.LeaveMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class LeaveTypeApplicationService implements LeaveTypeUseCase {

    private final LeaveTypeRepositoryPort leaveTypeRepository;
    private final LeaveMapper leaveMapper;

    @Override
    public Mono<Long> countTypes() {
        return leaveTypeRepository.count();
    }

    @Override
    public Flux<LeaveTypeDto> listTypes(Pageable p) {
        Pageable capped = PageRequest.of(
                Math.max(0, p.getPageNumber()),
                Math.min(p.getPageSize(), 50),
                p.getSort()
        );
        return leaveTypeRepository.findAll(capped).map(leaveMapper::toDto);
    }

    @Override
    @Transactional
    public Mono<LeaveTypeDto> createType(CreateLeaveTypeDto dto) {
        return leaveTypeRepository.existsByCodeIgnoreCase(dto.getCode())
                .flatMap(exists -> exists
                        ? Mono.error(new IllegalStateException("type code exists"))
                        : leaveTypeRepository.save(leaveMapper.toDomain(dto)).map(leaveMapper::toDto));
    }

    @Override
    @Transactional
    public Mono<LeaveTypeDto> updateType(UUID id, UpdateLeaveTypeDto dto) {
        return leaveTypeRepository.findById(id)
                .switchIfEmpty(Mono.error(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "type not found")))
                .map(e -> leaveMapper.updateLeaveType(dto, e))
                .flatMap(leaveTypeRepository::update)
                .map(leaveMapper::toDto);
    }

    @Override
    @Transactional
    public Mono<Void> deleteType(UUID id) {
        return leaveTypeRepository.findById(id)
                .switchIfEmpty(Mono.error(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "type not found")))
                .flatMap(found -> leaveTypeRepository.deleteById(id));
    }
}
