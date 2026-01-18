package com.khasanshin.fileservice.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FileMeta {

    UUID id;
    UUID ownerId;
    String ownerType;
    String category;
    String originalName;
    String contentType;
    long size;
    String storagePath;
    Instant uploadedAt;
    Instant updatedAt;
}
