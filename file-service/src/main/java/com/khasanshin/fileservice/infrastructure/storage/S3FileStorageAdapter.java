package com.khasanshin.fileservice.infrastructure.storage;

import com.khasanshin.fileservice.domain.port.FileStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
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
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3FileStorageAdapter implements FileStoragePort {

    private final S3Client s3Client;

    @Value("${s3.bucket}")
    private String bucketName;

    @Override
    public Mono<StoredObject> upload(String filename,
                                     Flux<DataBuffer> content,
                                     String contentTypeHint,
                                     UUID fileId) {
        return DataBufferUtils.join(content)
                .flatMap(dataBuffer -> {
                    long size = dataBuffer.readableByteCount();
                    String contentType = (contentTypeHint != null && !contentTypeHint.isBlank())
                            ? contentTypeHint
                            : "application/octet-stream";

                    return Mono.fromCallable(() -> {
                                LocalDate now = LocalDate.now();
                                String safeName = filename.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
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

                                return new StoredObject(key, size, contentType);
                            })
                            .subscribeOn(Schedulers.boundedElastic());
                });
    }

    @Override
    public Mono<Resource> download(String storagePath) {
        return Mono.fromCallable(() -> {
                    GetObjectRequest req = GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(storagePath)
                            .build();
                    ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(req);
                    return (Resource) new InputStreamResource(s3Object);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> delete(String storagePath) {
        return Mono.fromRunnable(() -> {
                    try {
                        s3Client.deleteObject(DeleteObjectRequest.builder()
                                .bucket(bucketName)
                                .key(storagePath)
                                .build());
                    } catch (Exception e) {
                        log.warn("Failed to delete S3 object {}", storagePath, e);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
