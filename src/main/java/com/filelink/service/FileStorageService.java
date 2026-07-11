package com.filelink.service;

import com.filelink.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootLocation;

    public FileStorageService(@Value("${filelink.upload-dir}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not initialize upload storage");
        }
    }

    /**
     * Saves the given multipart file to disk under a random UUID-based name
     * (keeping the original extension) and returns that stored name.
     */
    public String store(MultipartFile file) {
        String originalName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalName.substring(dotIndex);
        }
        String storedName = UUID.randomUUID() + extension;

        try {
            Path destination = rootLocation.resolve(storedName).normalize();
            if (!destination.getParent().equals(rootLocation)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid file path");
            }
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return storedName;
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file: " + e.getMessage());
        }
    }

    public Resource loadAsResource(String storedName) {
        try {
            Path filePath = rootLocation.resolve(storedName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ApiException(HttpStatus.NOT_FOUND, "File not found on server");
            }
        } catch (MalformedURLException e) {
            throw new ApiException(HttpStatus.NOT_FOUND, "File not found on server");
        }
    }

    public void delete(String storedName) {
        try {
            Path filePath = rootLocation.resolve(storedName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete file: " + e.getMessage());
        }
    }
}
