package com.khasanshin.fileservice.dto.event;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class FileDeletedEvent {

    UUID fileId;
    UUID ownerId;
    String ownerType;
    String category;
}
