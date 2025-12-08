package com.khasanshin.fileservice.controller;

import com.khasanshin.fileservice.dto.CreateFileMetaDto;
import com.khasanshin.fileservice.dto.FileDto;
import com.khasanshin.fileservice.dto.FileStreamResponseDto;
import com.khasanshin.fileservice.dto.FileUploadRequestDoc;
import com.khasanshin.fileservice.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.*;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(
        name = "File",
        description = "Загрузка, скачивание и список файлов"
)
public class FileController {

    private final FileService service;

    @Operation(
            summary = "Загрузить файл",
            description = "multipart/form-data: file, meta (JSON: owner_id, owner_type, category)"
    )
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(implementation = FileUploadRequestDoc.class),
                    encoding = {
                            @Encoding(name = "file", contentType = "application/octet-stream"),
                            @Encoding(name = "meta", contentType = "application/json")
                    }
            )
    )
    @ApiResponse(responseCode = "201")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public Mono<FileDto> upload(
            @RequestPart("file") FilePart file,
            @Valid @RequestPart("meta") CreateFileMetaDto meta) {
        return service.upload(file, meta);
    }

    @Operation(summary = "Скачать файл по id")
    @ApiResponse(responseCode = "200")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Mono<Void> download(
            @PathVariable("id") UUID id,
            ServerHttpResponse response) {

        return service.getMeta(id)
                .flatMap(meta -> {
                    response.getHeaders().setContentType(
                            MediaType.parseMediaType(meta.getContentType()));
                    response.getHeaders().set(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + meta.getOriginalName() + "\"");

                    Mono<Resource> resourceMono = service.loadAsResource(meta);

                    return resourceMono.flatMap(resource ->
                            response.writeWith(
                                    DataBufferUtils.read(
                                            resource,
                                            response.bufferFactory(),
                                            4096
                                    )));
                });
    }

    @Operation(summary = "Удалить файл")
    @ApiResponse(responseCode = "204")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public Mono<Void> delete(@PathVariable("id") UUID id) {
        return service.delete(id);
    }

    @Operation(
            summary = "Список файлов (пагинация)",
            description = "X-Total-Count — общее количество записей"
    )
    @ApiResponse(
            responseCode = "200",
            headers =
            @Header(
                    name = "X-Total-Count",
                    description = "Общее число записей",
                    schema = @Schema(type = "integer")))
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Flux<FileDto> list(
            @RequestParam(name = "owner_id", required = false) UUID ownerId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            ServerHttpResponse response) {

        return service.count(ownerId)
                .doOnNext(total ->
                        response.getHeaders()
                                .add("X-Total-Count", String.valueOf(total)))
                .thenMany(service.list(ownerId, page, size));
    }

    @Operation(
            summary = "Поток файлов (infinite scroll)",
            description = "Возвращает { items, has_next, next_cursor }"
    )
    @GetMapping("/stream")
    @PreAuthorize("isAuthenticated()")
    public Mono<FileStreamResponseDto> stream(
            @RequestParam(name = "owner_id", required = false) UUID ownerId,
            @RequestParam(name = "cursor", required = false) Instant cursor,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return service.stream(ownerId, cursor, size);
    }
}
