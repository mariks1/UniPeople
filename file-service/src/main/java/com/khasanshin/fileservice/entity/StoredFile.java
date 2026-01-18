package com.khasanshin.fileservice.entity;

import java.time.Instant;
import java.util.UUID;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "files")
public class StoredFile {

    @Id
    private UUID id;

    @Column("owner_id")
    private UUID ownerId;

    @Column("owner_type")
    private String ownerType;

    @Column("category")
    private String category;

    @Column("original_name")
    private String originalName;

    @Column("content_type")
    private String contentType;

    @Column("size")
    private long size;

    @Column("storage_path")
    private String storagePath;

    @CreatedDate
    @Column("uploaded_at")
    private Instant uploadedAt;

    @LastModifiedDate
    @Column("updated_at")
    private Instant updatedAt;
}
