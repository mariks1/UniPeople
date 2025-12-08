package com.khasanshin.fileservice.service;

import com.khasanshin.fileservice.dto.CreateFileMetaDto;
import com.khasanshin.fileservice.dto.FileDto;
import com.khasanshin.fileservice.dto.FileStreamResponseDto;
import com.khasanshin.fileservice.dto.event.FileDeletedEvent;
import com.khasanshin.fileservice.dto.event.FileUploadedEvent;
import com.khasanshin.fileservice.entity.StoredFile;
import com.khasanshin.fileservice.mapper.FileMapper;
import com.khasanshin.fileservice.repository.StoredFileRepository;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final StoredFileRepository repository;
    private final FileMapper mapper;
    private final FileEventPublisher eventPublisher;

    @Value("${files.storage-dir:/data/files}")
    private String storageDir;

    @Transactional
    public Mono<FileDto> upload(FilePart file, CreateFileMetaDto meta) {

        UUID fileId = UUID.randomUUID();
        Instant now = Instant.now();

        String ownerType = Optional.ofNullable(meta.getOwnerType()).orElse("EMPLOYEE");
        String category = Optional.ofNullable(meta.getCategory()).orElse("DOCUMENT");

        return saveToDisk(file, fileId)
                .flatMap(path -> {
                    StoredFile e = StoredFile.builder()
                            .id(fileId)
                            .ownerId(meta.getOwnerId())
                            .ownerType(ownerType)
                            .category(category)
                            .originalName(file.filename())
                            .contentType(
                                    Optional.ofNullable(file.headers().getContentType())
                                            .map(Object::toString)
                                            .orElse("application/octet-stream"))
                            .size(getContentLength(file))
                            .storagePath(path.toString())
                            .uploadedAt(now)
                            .build();

                    return repository.save(e);
                })
                .doOnSuccess(saved -> {
                    FileUploadedEvent event = FileUploadedEvent.builder()
                            .fileId(saved.getId())
                            .ownerId(saved.getOwnerId())
                            .ownerType(saved.getOwnerType())
                            .category(saved.getCategory())
                            .originalName(saved.getOriginalName())
                            .size(saved.getSize())
                            .uploadedAt(saved.getUploadedAt())
                            .build();
                    eventPublisher.publishFileUploaded(event);
                })
                .map(mapper::toDto);
    }

    public Mono<StoredFile> getMeta(UUID id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException("file not found")));
    }

    public Mono<Resource> loadAsResource(StoredFile meta) {
        Path path = Paths.get(meta.getStoragePath());
        return Mono.just(new FileSystemResource(path));
    }

    @Transactional
    public Mono<Void> delete(UUID id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException("file not found")))
                .flatMap(e ->
                        repository.deleteById(id)
                                .doOnSuccess(v -> {
                                    tryDeleteFromDisk(e.getStoragePath());
                                    FileDeletedEvent event = FileDeletedEvent.builder()
                                            .fileId(e.getId())
                                            .ownerId(e.getOwnerId())
                                            .ownerType(e.getOwnerType())
                                            .category(e.getCategory())
                                            .build();
                                    eventPublisher.publishFileDeleted(event);
                                }));
    }

    public Flux<FileDto> list(UUID ownerId, int page, int size) {
        int p = Math.max(0, page);
        int s = Math.min(Math.max(size, 1), 50);

        Flux<StoredFile> src = ownerId == null
                ? repository.findAllByOrderByUploadedAtDesc()
                : repository.findByOwnerIdOrderByUploadedAtDesc(ownerId);

        return src.skip((long) p * s).take(s).map(mapper::toDto);
    }

    public Mono<Long> count(UUID ownerId) {
        return ownerId == null ? repository.count() : repository.countByOwnerId(ownerId);
    }

    public Mono<FileStreamResponseDto> stream(UUID ownerId, Instant cursor, int size) {
        int s = Math.min(Math.max(size, 1), 50);
        Instant effectiveCursor = cursor != null ? cursor : Instant.now();

        Flux<StoredFile> src = ownerId == null
                ? repository.streamBefore(effectiveCursor)
                : repository.streamBeforeByOwner(ownerId, effectiveCursor);

        return src
                .take(s + 1)
                .map(mapper::toDto)
                .collectList()
                .map(list -> {
                    boolean hasNext = list.size() > s;
                    List<FileDto> items = hasNext ? list.subList(0, s) : list;
                    Instant nextCursor = null;
                    if (hasNext && !items.isEmpty()) {
                        nextCursor = items.getLast().getUploadedAt();
                    }
                    return FileStreamResponseDto.builder()
                            .items(items)
                            .hasNext(hasNext)
                            .nextCursor(nextCursor)
                            .build();
                });
    }

    private Mono<Path> saveToDisk(FilePart file, UUID fileId) {
        return Mono.fromCallable(() -> {
                    LocalDate now = LocalDate.now();
                    Path dir = Paths.get(
                            storageDir,
                            String.valueOf(now.getYear()),
                            String.valueOf(now.getMonthValue()),
                            String.valueOf(now.getDayOfMonth())
                    );
                    Files.createDirectories(dir);
                    String safeName = file.filename().replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
                    return dir.resolve(fileId + "-" + safeName);
                })
                .flatMap(path -> file.transferTo(path).thenReturn(path));
    }

    private void tryDeleteFromDisk(String storagePath) {
        try {
            Files.deleteIfExists(Paths.get(storagePath));
        } catch (Exception e) {
            log.warn("Failed to delete {}", storagePath, e);
        }
    }

    private long getContentLength(FilePart filePart) {
        var lengths = filePart.headers().get("Content-Length");
        if (lengths != null && !lengths.isEmpty()) {
            try {
                return Long.parseLong(lengths.get(0));
            } catch (NumberFormatException ignored) {}
        }
        return -1L;
    }
}
