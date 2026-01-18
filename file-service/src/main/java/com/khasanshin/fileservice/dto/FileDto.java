package com.khasanshin.fileservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;

@Data
@Builder
@Jacksonized
public class FileDto {

    @Id
    UUID id;

    @JsonAlias("owner_id")
    @JsonProperty("owner_id")
    UUID ownerId;

    @JsonAlias("owner_type")
    @JsonProperty("owner_type")
    String ownerType;

    @JsonAlias("category")
    @JsonProperty("category")
    String category;

    @JsonAlias("original_name")
    @JsonProperty("original_name")
    String originalName;

    @JsonAlias("content_type")
    @JsonProperty("content_type")
    String contentType;

    @JsonAlias("size")
    @JsonProperty("size")
    long size;

    @JsonAlias("uploaded_at")
    @JsonProperty("uploaded_at")
    Instant uploadedAt;

    @JsonAlias("updated_at")
    @JsonProperty("updated_at")
    Instant updatedAt;
}
