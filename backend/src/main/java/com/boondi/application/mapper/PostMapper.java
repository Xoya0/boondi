package com.boondi.application.mapper;

import com.boondi.application.dto.response.PostResponse;
import com.boondi.domain.entity.Post;
import com.boondi.domain.entity.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {

    public PostResponse toResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .author(toAuthorInfo(post.getAuthor()))
                .likeCount(post.getLikeCount())
                .repostCount(post.getRepostCount())
                .replyCount(post.getReplyCount())
                .bookmarkCount(post.getBookmarkCount())
                .parentPostId(parentPostId(post))
                .quotedPost(toQuotedPostInfo(post))
                .edited(post.isEdited())
                .editedAt(post.getEditedAt())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    private PostResponse.AuthorInfo toAuthorInfo(User user) {
        return PostResponse.AuthorInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .profilePictureUrl(user.getProfilePictureUrl())
                .build();
    }

    private java.util.UUID parentPostId(Post post) {
        try {
            return post.getParentPost() != null ? post.getParentPost().getId() : null;
        } catch (EntityNotFoundException e) {
            // Parent was soft-deleted — lazy proxy resolution fails under @SQLRestriction
            return null;
        }
    }

    private PostResponse.QuotedPostInfo toQuotedPostInfo(Post post) {
        try {
            Post quoted = post.getQuotedPost();
            if (quoted == null) return null;
            return PostResponse.QuotedPostInfo.builder()
                    .id(quoted.getId())
                    .content(quoted.getContent())
                    .imageUrl(quoted.getImageUrl())
                    .author(toAuthorInfo(quoted.getAuthor()))
                    .createdAt(quoted.getCreatedAt())
                    .build();
        } catch (EntityNotFoundException e) {
            // Quoted post was soft-deleted — render the quote without an embed
            return null;
        }
    }
}
