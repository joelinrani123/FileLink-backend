package com.filelink.dto;

import com.filelink.model.FileEntity;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class FileMetaResponse {
    private final Long id;
    private final String originalName;
    private final String contentType;
    private final long size;
    private final String shareToken;
    private final boolean isFolder;
    private final long downloadCount;
    private final String uploaderUsername;
    private final String createdAt;

    public FileMetaResponse(FileEntity entity) {
        this.id = entity.getId();
        this.originalName = entity.getOriginalName();
        this.contentType = entity.getContentType();
        this.size = entity.getSize();
        this.shareToken = entity.getShareToken();
        this.isFolder = entity.isFolder();
        this.downloadCount = entity.getDownloadCount();
        this.uploaderUsername = entity.getUploader().getUsername();
        LocalDateTime created = entity.getCreatedAt();
        this.createdAt = created == null ? null : created.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
