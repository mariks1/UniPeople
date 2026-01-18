package com.khasanshin.fileservice.repository;

import com.khasanshin.fileservice.entity.StoredFile;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StoredFileRepository extends ReactiveCrudRepository<StoredFile, UUID> {

    Flux<StoredFile> findAllByOrderByUploadedAtDesc();

    Flux<StoredFile> findByOwnerIdOrderByUploadedAtDesc(UUID ownerId);

    @Query("""
      SELECT * FROM files
      WHERE uploaded_at < :cursor
      ORDER BY uploaded_at DESC
      """)
    Flux<StoredFile> streamBefore(Instant cursor);

    @Query("""
      SELECT * FROM files
      WHERE owner_id = :ownerId
        AND uploaded_at < :cursor
      ORDER BY uploaded_at DESC
      """)
    Flux<StoredFile> streamBeforeByOwner(UUID ownerId, Instant cursor);

    Mono<Long> count();

    Mono<Long> countByOwnerId(UUID ownerId);
}
