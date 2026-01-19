package com.khasanshin.fileservice.domain.port;

import com.khasanshin.fileservice.domain.model.FileMeta;
import java.time.Instant;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FileMetaRepositoryPort {

    Mono<FileMeta> save(FileMeta meta);

    Mono<FileMeta> findById(UUID id);

    Mono<Void> deleteById(UUID id);

    Flux<FileMeta> findAllOrderByUploadedAtDesc();

    Flux<FileMeta> findByOwnerOrderByUploadedAtDesc(UUID ownerId);

    Mono<Long> count();

    Mono<Long> countByOwner(UUID ownerId);

    Flux<FileMeta> streamBefore(Instant cursor);

    Flux<FileMeta> streamBeforeByOwner(UUID ownerId, Instant cursor);
}
