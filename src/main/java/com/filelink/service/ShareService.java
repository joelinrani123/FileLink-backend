package com.filelink.service;

import com.filelink.exception.ApiException;
import com.filelink.model.FileEntity;
import com.filelink.model.SharedFile;
import com.filelink.model.User;
import com.filelink.repository.SharedFileRepository;
import com.filelink.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShareService {

    private final SharedFileRepository sharedFileRepository;
    private final UserRepository userRepository;
    private final FileShareService fileShareService;

    public ShareService(SharedFileRepository sharedFileRepository, UserRepository userRepository,
                         FileShareService fileShareService) {
        this.sharedFileRepository = sharedFileRepository;
        this.userRepository = userRepository;
        this.fileShareService = fileShareService;
    }

    public SharedFile share(Long senderId, String fileToken, String recipientUsername) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));

        User recipient = userRepository.findByUsername(recipientUsername.trim())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No user found with that username"));

        if (recipient.getId().equals(sender.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You can't send a link to yourself");
        }

        FileEntity file = fileShareService.getByToken(fileToken);

        SharedFile sharedFile = new SharedFile();
        sharedFile.setFile(file);
        sharedFile.setSender(sender);
        sharedFile.setRecipient(recipient);
        return sharedFileRepository.save(sharedFile);
    }

    public List<SharedFile> received(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
        return sharedFileRepository.findByRecipientOrderByCreatedAtDesc(user);
    }

    public List<SharedFile> sent(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
        return sharedFileRepository.findBySenderOrderByCreatedAtDesc(user);
    }

    public void dismiss(Long userId, Long sharedFileId) {
        SharedFile sharedFile = sharedFileRepository.findById(sharedFileId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Shared item not found"));
        if (!sharedFile.getRecipient().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only remove items shared with you");
        }
        sharedFileRepository.delete(sharedFile);
    }

    public List<User> searchUsers(Long currentUserId, String query) {
        if (query == null || query.trim().length() < 1) {
            return List.of();
        }
        return userRepository.findTop10ByUsernameContainingIgnoreCaseAndIdNot(query.trim(), currentUserId);
    }
}
