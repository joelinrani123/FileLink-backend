package com.filelink.controller;

import com.filelink.dto.ShareRequest;
import com.filelink.dto.SharedFileResponse;
import com.filelink.dto.UserSummaryResponse;
import com.filelink.security.AuthContext;
import com.filelink.service.ShareService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ShareController {

    private final ShareService shareService;

    public ShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    @GetMapping("/users/search")
    public List<UserSummaryResponse> searchUsers(HttpServletRequest request, @RequestParam("q") String query) {
        Long userId = AuthContext.requireUserId(request);
        return shareService.searchUsers(userId, query).stream()
                .map(UserSummaryResponse::new)
                .collect(Collectors.toList());
    }

    @PostMapping("/shares")
    public SharedFileResponse share(HttpServletRequest request, @Valid @RequestBody ShareRequest shareRequest) {
        Long userId = AuthContext.requireUserId(request);
        return new SharedFileResponse(
                shareService.share(userId, shareRequest.getFileToken(), shareRequest.getRecipientUsername())
        );
    }

    @GetMapping("/shares/received")
    public List<SharedFileResponse> received(HttpServletRequest request) {
        Long userId = AuthContext.requireUserId(request);
        return shareService.received(userId).stream()
                .map(SharedFileResponse::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/shares/sent")
    public List<SharedFileResponse> sent(HttpServletRequest request) {
        Long userId = AuthContext.requireUserId(request);
        return shareService.sent(userId).stream()
                .map(SharedFileResponse::new)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/shares/{id}")
    public Map<String, String> dismiss(HttpServletRequest request, @PathVariable Long id) {
        Long userId = AuthContext.requireUserId(request);
        shareService.dismiss(userId, id);
        return Map.of("message", "Removed from your inbox");
    }
}
