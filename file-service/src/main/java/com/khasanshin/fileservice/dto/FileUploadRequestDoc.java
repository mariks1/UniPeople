package com.khasanshin.fileservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileUploadRequestDoc {

    @Schema(type = "string", format = "binary", description = "Загружаемый файл")
    private MultipartFile file;

    @Schema(description = "Метаданные файла (JSON)")
    private CreateFileMetaDto meta;
}
