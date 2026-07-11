package com.filelink.security;

import com.filelink.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

public class AuthContext {

    private AuthContext() {
    }

    public static Long requireUserId(HttpServletRequest request) {
        Object userId = request.getAttribute(JwtAuthFilter.ATTR_USER_ID);
        if (userId == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "You must be logged in to perform this action");
        }
        return (Long) userId;
    }
}
