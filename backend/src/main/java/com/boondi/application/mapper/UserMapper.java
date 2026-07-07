package com.boondi.application.mapper;

import com.boondi.application.dto.response.UserResponse;
import com.boondi.domain.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        LocalDateTime createdAt = user.getCreatedAt() != null
                ? user.getCreatedAt().toLocalDateTime()
                : null;

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .bio(user.getBio())
                .profilePictureUrl(user.getProfilePictureUrl())
                .bannerImageUrl(user.getBannerImageUrl())
                .role(user.getRole())
                .emailVerified(user.isEmailVerified())
                .suspended(user.isSuspended())
                .followerCount(user.getFollowerCount())
                .followingCount(user.getFollowingCount())
                .postCount(user.getPostCount())
                .createdAt(createdAt)
                .build();
    }
}
