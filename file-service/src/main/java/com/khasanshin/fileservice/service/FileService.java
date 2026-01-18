package com.khasanshin.fileservice.service;

import com.khasanshin.fileservice.dto.CreateFileMetaDto;
import com.khasanshin.fileservice.dto.FileDto;
import com.khasanshin.fileservice.dto.FileStreamResponseDto;
import com.khasanshin.fileservice.entity.StoredFile;
import com.khasanshin.fileservice.mapper.FileMapper;
import com.khasanshin.fileservice.repository.StoredFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final StoredFileRepository repository;
    private final FileMapper mapper;
    private final S3Client s3Client;

    @Value("${s3.bucket}")
    private String bucketName;

    @Transactional
    public Mono<FileDto> upload(FilePart file, CreateFileMetaDto meta) {

        Instant now = Instant.now();
        String ownerType = Optional.ofNullable(meta.getOwnerType()).orElse("EMPLOYEE");
        String category = Optional.ofNullable(meta.getCategory()).orElse("DOCUMENT");

        UUID pathId = UUID.randomUUID();

        return uploadToS3(file, pathId)
                .flatMap(objInfo -> {
                    StoredFile e = StoredFile.builder()
                            .ownerId(meta.getOwnerId())
                            .ownerType(ownerType)
                            .category(category)
                            .originalName(file.filename())
                            .contentType(objInfo.contentType())
                            .size(objInfo.size())
                            .storagePath(objInfo.key())
                            .uploadedAt(now)
                            .build();

                    return repository.save(e);
                })
                .map(mapper::toDto);
    }

    public Mono<StoredFile> getMeta(UUID id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException("file not found")));
    }

    public Mono<Resource> loadAsResource(StoredFile meta) {
        return Mono.fromCallable(() -> {
                    GetObjectRequest req = GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(meta.getStoragePath())
                            .build();
                    ResponseInputStream<GetObjectResponse> s3Object =
                            s3Client.getObject(req);
                    return (Resource) new InputStreamResource(s3Object);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<Void> delete(UUID id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException("file not found")))
                .flatMap(e ->
                        deleteFromS3(e.getStoragePath())
                                .then(repository.deleteById(id)));
    }

    public Flux<FileDto> list(UUID ownerId, int page, int size) {
        int p = Math.max(0, page);
        int s = Math.min(Math.max(size, 1), 50);

        Flux<StoredFile> src = ownerId == null
                ? repository.findAllByOrderByUploadedAtDesc()
                : repository.findByOwnerIdOrderByUploadedAtDesc(ownerId);

        return src
                .skip((long) p * s)
                .take(s)
                .map(mapper::toDto);
    }

    public Mono<Long> count(UUID ownerId) {
        return ownerId == null
                ? repository.count()
                : repository.countByOwnerId(ownerId);
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


    private record S3ObjectInfo(String key, long size, String contentType) {}

    private Mono<S3ObjectInfo> uploadToS3(FilePart file, UUID fileId) {
        return DataBufferUtils.join(file.content())
                .flatMap(dataBuffer -> {
                    long size = dataBuffer.readableByteCount();
                    String contentType = Optional.ofNullable(file.headers().getContentType())
                            .map(Object::toString)
                            .orElse("application/octet-stream");

                    return Mono.fromCallable(() -> {
                                LocalDate now = LocalDate.now();
                                String safeName = file.filename().replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
                                String key = "%d/%02d/%02d/%s-%s".formatted(
                                        now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
                                        fileId, safeName);

                                PutObjectRequest req = PutObjectRequest.builder()
                                        .bucket(bucketName)
                                        .key(key)
                                        .contentType(contentType)
                                        .contentLength(size)
                                        .build();

                                try (InputStream is = dataBuffer.asInputStream(true)) {
                                    s3Client.putObject(req, RequestBody.fromInputStream(is, size));
                                }

                                return new S3ObjectInfo(key, size, contentType);
                            })
                            .subscribeOn(Schedulers.boundedElastic());
                });
    }

    private Mono<Void> deleteFromS3(String key) {
        return Mono.fromRunnable(() -> {
                    try {
                        s3Client.deleteObject(DeleteObjectRequest.builder()
                                .bucket(bucketName)
                                .key(key)
                                .build());
                    } catch (Exception e) {
                        log.warn("Failed to delete S3 object {}", key, e);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
