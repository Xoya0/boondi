package com.boondi.application.service;

import com.boondi.application.dto.request.UpdateProfileRequest;
import com.boondi.application.dto.response.UploadResponse;
import com.boondi.application.dto.response.UserResponse;
import com.boondi.application.mapper.UserMapper;
import com.boondi.domain.entity.User;
import com.boondi.domain.repository.FollowRepository;
import com.boondi.domain.repository.UserRepository;
import com.boondi.infrastructure.exception.BoondiException;
import com.boondi.infrastructure.service.ImageContentValidator;
import com.boondi.infrastructure.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final List<String> ALLOWED_IMAGE_TYPES =
            List.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_AVATAR_SIZE = 5L * 1024 * 1024;   // 5 MB
    private static final long MAX_BANNER_SIZE = 10L * 1024 * 1024;  // 10 MB

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final StorageService storageService;
    private final FollowRepository followRepository;
    private final ImageContentValidator imageContentValidator;

    @Transactional(readOnly = true)
    public UserResponse getProfile(String username, UUID viewerId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BoondiException.userNotFound(username));
        UserResponse response = userMapper.toResponse(user);
        if (viewerId != null && !viewerId.equals(user.getId())) {
            response.setFollowedByViewer(
                    followRepository.existsByFollowerIdAndFolloweeId(viewerId, user.getId()));
        }
        return response;
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BoondiException.userNotFound(userId.toString()));

        if (StringUtils.hasText(request.getDisplayName())) {
            user.setDisplayName(request.getDisplayName());
        }
        if (request.getBio() != null) {  // allow clearing bio with empty string
            user.setBio(request.getBio());
        }
        if (StringUtils.hasText(request.getUsername())) {
            String newUsername = request.getUsername();
            if (!newUsername.equals(user.getUsername())) {
                if (userRepository.existsByUsername(newUsername)) {
                    throw BoondiException.usernameAlreadyExists();
                }
                user.setUsername(newUsername);
            }
        }

        User saved = userRepository.save(user);
        log.info("Profile updated for userId={}", userId);
        return userMapper.toResponse(saved);
    }

    @Transactional
    public UploadResponse updateAvatar(UUID userId, MultipartFile file) {
        validateImage(file, MAX_AVATAR_SIZE);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> BoondiException.userNotFound(userId.toString()));

        String extension = extensionFromContentType(file.getContentType());
        String key = "avatars/" + userId + "/" + UUID.randomUUID() + "." + extension;

        try {
            String url = storageService.uploadFile(key, file.getInputStream(),
                    file.getSize(), file.getContentType());
            user.setProfilePictureUrl(url);
            userRepository.save(user);
            log.info("Avatar updated for userId={}", userId);
            return UploadResponse.of(url);
        } catch (BoondiException e) {
            throw e;
        } catch (Exception e) {
            throw BoondiException.fileUploadFailed(e.getMessage());
        }
    }

    @Transactional
    public UploadResponse updateBanner(UUID userId, MultipartFile file) {
        validateImage(file, MAX_BANNER_SIZE);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> BoondiException.userNotFound(userId.toString()));

        String extension = extensionFromContentType(file.getContentType());
        String key = "banners/" + userId + "/" + UUID.randomUUID() + "." + extension;

        try {
            String url = storageService.uploadFile(key, file.getInputStream(),
                    file.getSize(), file.getContentType());
            user.setBannerImageUrl(url);
            userRepository.save(user);
            log.info("Banner updated for userId={}", userId);
            return UploadResponse.of(url);
        } catch (BoondiException e) {
            throw e;
        } catch (Exception e) {
            throw BoondiException.fileUploadFailed(e.getMessage());
        }
    }

    private void validateImage(MultipartFile file, long maxSize) {
        if (file == null || file.isEmpty()) {
            throw BoondiException.fileUploadFailed("No file provided");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)
                || !imageContentValidator.matches(file, contentType)) {
            throw BoondiException.invalidFileType();
        }
        if (file.getSize() > maxSize) {
            throw BoondiException.fileTooLarge(maxSize);
        }
    }

    private String extensionFromContentType(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png"  -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }
}
