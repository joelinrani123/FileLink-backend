package com.filelink.repository;

import com.filelink.model.FileEntity;
import com.filelink.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    Optional<FileEntity> findByShareToken(String shareToken);
    boolean existsByShareToken(String shareToken);
    List<FileEntity> findByUploaderOrderByCreatedAtDesc(User uploader);
}
