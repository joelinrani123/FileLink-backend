package com.filelink.controller;

import com.filelink.dto.FileMetaResponse;
import com.filelink.model.FileEntity;
import com.filelink.security.AuthContext;
import com.filelink.service.FileShareService;
import com.filelink.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileShareService fileShareService;
    private final FileStorageService fileStorageService;

    public FileController(FileShareService fileShareService, FileStorageService fileStorageService) {
        this.fileShareService = fileShareService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public FileMetaResponse upload(HttpServletRequest request,
                                    @RequestParam("file") MultipartFile file,
                                    @RequestParam(value = "isFolder", defaultValue = "false") boolean isFolder,
                                    @RequestParam(value = "displayName", required = false) String displayName) {
        Long userId = AuthContext.requireUserId(request);
        FileEntity entity = fileShareService.upload(userId, file, isFolder, displayName);
        return new FileMetaResponse(entity);
    }

    @GetMapping("/meta/{token}")
    public FileMetaResponse meta(@PathVariable String token) {
        return new FileMetaResponse(fileShareService.getByToken(token));
    }

    @GetMapping("/download/{token}")
    public ResponseEntity<Resource> download(@PathVariable String token) {
        FileEntity entity = fileShareService.getByToken(token);
        Resource resource = fileStorageService.loadAsResource(entity.getStoredName());
        fileShareService.incrementDownloadCount(entity);

        String encodedName = java.net.URLEncoder.encode(entity.getOriginalName(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(entity.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .body(resource);
    }

    @GetMapping("/my")
    public List<FileMetaResponse> myFiles(HttpServletRequest request) {
        Long userId = AuthContext.requireUserId(request);
        return fileShareService.listForUser(userId).stream()
                .map(FileMetaResponse::new)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{token}")
    public Map<String, String> delete(HttpServletRequest request, @PathVariable String token) {
        Long userId = AuthContext.requireUserId(request);
        fileShareService.delete(userId, token);
        return Map.of("message", "File deleted successfully");
    }
}
