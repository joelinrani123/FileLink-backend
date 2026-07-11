package com.filelink.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "files", uniqueConstraints = @UniqueConstraint(columnNames = "share_token"))
@Getter
@Setter
@NoArgsConstructor
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalName;

    // Name of the file as it is physically stored on disk (UUID based, collision-safe)
    @Column(nullable = false)
    private String storedName;

    // Cloudinary secure URL for downloading the file
    @Column(nullable = false, length = 500)
    private String fileUrl;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private long size;

    @Column(name = "share_token", nullable = false, unique = true, length = 20)
    private String shareToken;

    @Column(nullable = false)
    private boolean isFolder = false;

    @Column(nullable = false)
    private long downloadCount = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}