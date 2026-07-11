package com.filelink.service;

import com.filelink.exception.ApiException;
import com.filelink.model.FileEntity;
import com.filelink.model.User;
import com.filelink.repository.FileRepository;
import com.filelink.repository.UserRepository;
import com.filelink.util.TokenGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class FileShareService {

    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public FileShareService(FileRepository fileRepository, UserRepository userRepository,
                            FileStorageService fileStorageService) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    public FileEntity upload(Long userId, MultipartFile file, boolean isFolder, String displayName) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No file was provided");
        }
        User uploader = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));

        FileStorageService.StoredFile stored = fileStorageService.store(file);

        FileEntity entity = new FileEntity();
        entity.setOriginalName(displayName != null && !displayName.isBlank() ? displayName : file.getOriginalFilename());
        entity.setStoredName(stored.getPublicId());
        entity.setFileUrl(stored.getUrl());
        entity.setContentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
        entity.setSize(file.getSize());
        entity.setFolder(isFolder);
        entity.setUploader(uploader);
        entity.setShareToken(generateUniqueToken());

        return fileRepository.save(entity);
    }

    private String generateUniqueToken() {
        String token;
        do {
            token = TokenGenerator.generate(8);
        } while (fileRepository.existsByShareToken(token));
        return token;
    }

    public FileEntity getByToken(String token) {
        return fileRepository.findByShareToken(token)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No file found for this link"));
    }

    public void incrementDownloadCount(FileEntity entity) {
        entity.setDownloadCount(entity.getDownloadCount() + 1);
        fileRepository.save(entity);
    }

    public List<FileEntity> listForUser(Long userId) {
        User uploader = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
        return fileRepository.findByUploaderOrderByCreatedAtDesc(uploader);
    }

    public void delete(Long userId, String token) {
        FileEntity entity = getByToken(token);
        if (!entity.getUploader().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only delete your own files");
        }
        fileStorageService.delete(entity.getStoredName());
        fileRepository.delete(entity);
    }
}