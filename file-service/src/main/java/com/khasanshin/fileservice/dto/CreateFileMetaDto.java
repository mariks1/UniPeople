package com.khasanshin.fileservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class CreateFileMetaDto {

    @NotNull
    @JsonAlias("owner_id")
    @JsonProperty("owner_id")
    UUID ownerId;

    @Size(max = 50)
    @JsonAlias("owner_type")
    @JsonProperty("owner_type")
    String ownerType;

    @Size(max = 50)
    @JsonAlias("category")
    @JsonProperty("category")
    String category;
}
