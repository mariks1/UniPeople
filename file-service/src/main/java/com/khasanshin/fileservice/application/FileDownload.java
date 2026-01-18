package com.khasanshin.fileservice.application;

import com.khasanshin.fileservice.domain.model.FileMeta;
import org.springframework.core.io.Resource;

public record FileDownload(FileMeta meta, Resource resource) {}
