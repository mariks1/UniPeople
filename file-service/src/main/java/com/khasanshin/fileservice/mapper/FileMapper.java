package com.khasanshin.fileservice.mapper;

import com.khasanshin.fileservice.dto.FileDto;
import com.khasanshin.fileservice.entity.StoredFile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FileMapper {

    FileDto toDto(StoredFile e);
}
