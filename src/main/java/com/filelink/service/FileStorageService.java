package com.filelink.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.filelink.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Cloudinary cloudinary;

    public FileStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Uploads the given multipart file to Cloudinary under a random UUID-based
     * public_id and returns the stored public_id and the secure download URL.
     */
    public StoredFile store(MultipartFile file) {
        String publicId = "filelink/" + UUID.randomUUID();
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "public_id", publicId,
                    "resource_type", "auto",
                    "use_filename", false,
                    "unique_filename", false
            ));
            String secureUrl = (String) result.get("secure_url");
            return new StoredFile(publicId, secureUrl);
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file: " + e.getMessage());
        }
    }

    public void delete(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "auto"));
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete file: " + e.getMessage());
        }
    }

    public static class StoredFile {
        private final String publicId;
        private final String url;

        public StoredFile(String publicId, String url) {
            this.publicId = publicId;
            this.url = url;
        }

        public String getPublicId() {
            return publicId;
        }

        public String getUrl() {
            return url;
        }
    }
}