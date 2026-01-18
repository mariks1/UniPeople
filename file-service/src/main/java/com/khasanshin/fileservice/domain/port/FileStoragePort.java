package com.khasanshin.fileservice.domain.port;

import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FileStoragePort {

    Mono<StoredObject> upload(String filename,
                              Flux<DataBuffer> content,
                              String contentTypeHint,
                              UUID fileId);

    Mono<Resource> download(String storagePath);

    Mono<Void> delete(String storagePath);

    record StoredObject(String key, long size, String contentType) {}
}
