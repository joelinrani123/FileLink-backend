package com.filelink.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShareRequest {

    @NotBlank(message = "fileToken is required")
    private String fileToken;

    @NotBlank(message = "recipientUsername is required")
    private String recipientUsername;
}
