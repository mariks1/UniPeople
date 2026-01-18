package com.khasanshin.fileservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class FileStreamResponseDto {

    @JsonAlias("items")
    @JsonProperty("items")
    List<FileDto> items;

    @JsonAlias("has_next")
    @JsonProperty("has_next")
    boolean hasNext;

    @JsonAlias("next_cursor")
    @JsonProperty("next_cursor")
    Instant nextCursor;
}
