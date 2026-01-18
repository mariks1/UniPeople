package com.khasanshin.fileservice.application;

import com.khasanshin.fileservice.dto.CreateFileMetaDto;
import com.khasanshin.fileservice.dto.FileDto;
import com.khasanshin.fileservice.dto.FileStreamResponseDto;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FileUseCase {

    Mono<FileDto> upload(FilePart file, CreateFileMetaDto meta);

    Mono<FileDownload> download(UUID id);

    Mono<Void> delete(UUID id);

    Flux<FileDto> list(UUID ownerId, int page, int size);

    Mono<Long> count(UUID ownerId);

    Mono<FileStreamResponseDto> stream(UUID ownerId, Instant cursor, int size);
}
