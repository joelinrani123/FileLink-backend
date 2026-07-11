package com.filelink.dto;

import com.filelink.model.User;
import lombok.Getter;

@Getter
public class UserSummaryResponse {
    private final String username;

    public UserSummaryResponse(User user) {
        this.username = user.getUsername();
    }
}
