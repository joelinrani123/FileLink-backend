package com.filelink.repository;

import com.filelink.model.FileEntity;
import com.filelink.model.SharedFile;
import com.filelink.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SharedFileRepository extends JpaRepository<SharedFile, Long> {
    List<SharedFile> findByRecipientOrderByCreatedAtDesc(User recipient);
    List<SharedFile> findBySenderOrderByCreatedAtDesc(User sender);
    Optional<SharedFile> findByFileAndRecipient(FileEntity file, User recipient);
}
