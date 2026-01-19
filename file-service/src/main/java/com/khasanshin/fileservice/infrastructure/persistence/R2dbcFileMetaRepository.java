package com.khasanshin.fileservice.infrastructure.persistence;

import com.khasanshin.fileservice.domain.model.FileMeta;
import com.khasanshin.fileservice.domain.port.FileMetaRepositoryPort;
import com.khasanshin.fileservice.entity.StoredFile;
import com.khasanshin.fileservice.repository.StoredFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class R2dbcFileMetaRepository implements FileMetaRepositoryPort {

    private final StoredFileRepository repository;

    @Override
    public Mono<FileMeta> save(FileMeta meta) {
        return repository.save(toEntity(meta)).map(this::toDomain);
    }

    @Override
    public Mono<FileMeta> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return repository.deleteById(id);
    }

    @Override
    public Flux<FileMeta> findAllOrderByUploadedAtDesc() {
        return repository.findAllByOrderByUploadedAtDesc().map(this::toDomain);
    }

    @Override
    public Flux<FileMeta> findByOwnerOrderByUploadedAtDesc(UUID ownerId) {
        return repository.findByOwnerIdOrderByUploadedAtDesc(ownerId).map(this::toDomain);
    }

    @Override
    public Mono<Long> count() {
        return repository.count();
    }

    @Override
    public Mono<Long> countByOwner(UUID ownerId) {
        return repository.countByOwnerId(ownerId);
    }

    @Override
    public Flux<FileMeta> streamBefore(Instant cursor) {
        return repository.streamBefore(cursor).map(this::toDomain);
    }

    @Override
    public Flux<FileMeta> streamBeforeByOwner(UUID ownerId, Instant cursor) {
        return repository.streamBeforeByOwner(ownerId, cursor).map(this::toDomain);
    }

    private StoredFile toEntity(FileMeta meta) {
        return StoredFile.builder()
                .id(meta.getId())
                .ownerId(meta.getOwnerId())
                .ownerType(meta.getOwnerType())
                .category(meta.getCategory())
                .originalName(meta.getOriginalName())
                .contentType(meta.getContentType())
                .size(meta.getSize())
                .storagePath(meta.getStoragePath())
                .uploadedAt(meta.getUploadedAt())
                .updatedAt(meta.getUpdatedAt())
                .build();
    }

    private FileMeta toDomain(StoredFile e) {
        return FileMeta.builder()
                .id(e.getId())
                .ownerId(e.getOwnerId())
                .ownerType(e.getOwnerType())
                .category(e.getCategory())
                .originalName(e.getOriginalName())
                .contentType(e.getContentType())
                .size(e.getSize())
                .storagePath(e.getStoragePath())
                .uploadedAt(e.getUploadedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
