package com.khasanshin.fileservice.dto.event;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class FileUploadedEvent {

    UUID fileId;
    UUID ownerId;
    String ownerType;
    String category;
    String originalName;
    long size;
    Instant uploadedAt;
}
