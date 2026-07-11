package com.filelink.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Lightweight auth filter. It never blocks a request itself - it simply
 * decodes a Bearer token (if present) and stashes the userId/username as
 * request attributes. Controllers that require authentication check for
 * these attributes and reject the request with 401 if missing.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    public static final String ATTR_USER_ID = "authUserId";
    public static final String ATTR_USERNAME = "authUsername";

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwtUtil.parseClaims(token);
                Long userId = claims.get("userId", Integer.class) != null
                        ? Long.valueOf(claims.get("userId", Integer.class))
                        : claims.get("userId", Long.class);
                request.setAttribute(ATTR_USER_ID, userId);
                request.setAttribute(ATTR_USERNAME, claims.getSubject());
            } catch (Exception ignored) {
                // invalid/expired token -> leave attributes unset, controller will reject if auth required
            }
        }
        filterChain.doFilter(request, response);
    }
}
