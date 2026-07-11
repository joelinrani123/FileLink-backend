package com.filelink.dto;

import com.filelink.model.SharedFile;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class SharedFileResponse {
    private final Long id;
    private final FileMetaResponse file;
    private final String senderUsername;
    private final String recipientUsername;
    private final String sharedAt;

    public SharedFileResponse(SharedFile sharedFile) {
        this.id = sharedFile.getId();
        this.file = new FileMetaResponse(sharedFile.getFile());
        this.senderUsername = sharedFile.getSender().getUsername();
        this.recipientUsername = sharedFile.getRecipient().getUsername();
        this.sharedAt = sharedFile.getCreatedAt() == null
                ? null
                : sharedFile.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
