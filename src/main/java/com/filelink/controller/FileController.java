package com.filelink.controller;

import com.filelink.dto.FileMetaResponse;
import com.filelink.model.FileEntity;
import com.filelink.security.AuthContext;
import com.filelink.service.FileShareService;
import com.filelink.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<Void> download(@PathVariable String token) {
        FileEntity entity = fileShareService.getByToken(token);
        fileShareService.incrementDownloadCount(entity);

        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, entity.getFileUrl())
                .build();
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