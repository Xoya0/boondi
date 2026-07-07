package com.boondi.application.service;

import com.boondi.application.dto.request.CreatePostRequest;
import com.boondi.application.dto.request.UpdatePostRequest;
import com.boondi.application.dto.response.PostResponse;
import com.boondi.application.dto.response.UploadResponse;
import com.boondi.application.mapper.PostMapper;
import com.boondi.domain.entity.Hashtag;
import com.boondi.domain.entity.Post;
import com.boondi.domain.entity.PostHashtag;
import com.boondi.domain.entity.User;
import com.boondi.domain.repository.HashtagRepository;
import com.boondi.domain.repository.PostHashtagRepository;
import com.boondi.domain.repository.PostRepository;
import com.boondi.domain.repository.UserRepository;
import com.boondi.infrastructure.exception.BoondiException;
import com.boondi.infrastructure.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private static final int EDIT_WINDOW_MINUTES = 30;
    private static final List<String> ALLOWED_IMAGE_TYPES =
            List.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_POST_IMAGE_SIZE = 5L * 1024 * 1024; // 5 MB
    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#(\\w+)");

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostMapper postMapper;
    private final StorageService storageService;
    private final PostViewerStateService postViewerStateService;
    private final TimelineCacheService timelineCacheService;
    private final NotificationService notificationService;
    private final HashtagRepository hashtagRepository;
    private final PostHashtagRepository postHashtagRepository;

    @Transactional
    public PostResponse createPost(UUID authorId, CreatePostRequest request) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> BoondiException.userNotFound(authorId.toString()));

        // Reply (E6-08): a reply is a regular post with a parent FK
        Post parentPost = null;
        if (request.getParentPostId() != null) {
            parentPost = postRepository.findById(request.getParentPostId())
                    .orElseThrow(() -> BoondiException.postNotFound(request.getParentPostId().toString()));
            if (parentPost.getDeletedAt() != null) {
                throw BoondiException.postNotFound(request.getParentPostId().toString());
            }
        }

        // Quote (E6-03): a quote is a regular post embedding another post
        Post quotedPost = null;
        if (request.getQuotedPostId() != null) {
            quotedPost = postRepository.findById(request.getQuotedPostId())
                    .orElseThrow(() -> BoondiException.postNotFound(request.getQuotedPostId().toString()));
            if (quotedPost.getDeletedAt() != null) {
                throw BoondiException.postNotFound(request.getQuotedPostId().toString());
            }
        }

        Post post = Post.builder()
                .author(author)
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .parentPost(parentPost)
                .quotedPost(quotedPost)
                .build();

        Post saved = postRepository.save(post);

        author.setPostCount(author.getPostCount() + 1);
        userRepository.save(author);

        if (parentPost != null) {
            parentPost.setReplyCount(parentPost.getReplyCount() + 1);
            postRepository.save(parentPost);
            notificationService.notifyReply(authorId, parentPost);
        }
        if (quotedPost != null) {
            // Quotes count toward the quoted post's repost total
            quotedPost.setRepostCount(quotedPost.getRepostCount() + 1);
            postRepository.save(quotedPost);
            notificationService.notifyRepost(authorId, quotedPost);
        }

        extractAndLinkHashtags(saved);

        // Followers' cached home timelines are now stale
        timelineCacheService.evictFollowersOf(authorId);

        log.info("Post created: postId={}, authorId={}, reply={}, quote={}",
                saved.getId(), authorId, parentPost != null, quotedPost != null);
        return postMapper.toResponse(saved);
    }

    // Hashtag extraction (E8-04): parse #word patterns, find-or-create the Hashtag,
    // and link it to the post. Tags are stored lowercase; duplicates within the same
    // post content are deduplicated via the Set before insert.
    private void extractAndLinkHashtags(Post post) {
        Matcher matcher = HASHTAG_PATTERN.matcher(post.getContent());
        Set<String> tags = new LinkedHashSet<>();
        while (matcher.find()) {
            tags.add(matcher.group(1).toLowerCase());
        }

        for (String tag : tags) {
            Hashtag hashtag = hashtagRepository.findByTag(tag)
                    .orElseGet(() -> hashtagRepository.save(Hashtag.builder().tag(tag).build()));
            postHashtagRepository.save(PostHashtag.builder()
                    .postId(post.getId())
                    .hashtagId(hashtag.getId())
                    .build());
        }
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(UUID postId, UUID viewerId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> BoondiException.postNotFound(postId.toString()));
        PostResponse response = postMapper.toResponse(post);
        postViewerStateService.enrich(viewerId, response);
        return response;
    }

    @Transactional
    public PostResponse updatePost(UUID postId, UUID userId, UpdatePostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> BoondiException.postNotFound(postId.toString()));

        if (!post.getAuthor().getId().equals(userId)) {
            throw BoondiException.postAccessDenied();
        }

        OffsetDateTime editDeadline = post.getCreatedAt().plusMinutes(EDIT_WINDOW_MINUTES);
        if (OffsetDateTime.now().isAfter(editDeadline)) {
            throw BoondiException.postEditWindowExpired();
        }

        post.setContent(request.getContent());
        if (request.getImageUrl() != null) {
            post.setImageUrl(request.getImageUrl());
        }
        post.setEdited(true);
        post.setEditedAt(OffsetDateTime.now());

        Post saved = postRepository.save(post);
        log.info("Post updated: postId={}", postId);
        return postMapper.toResponse(saved);
    }

    @Transactional
    public void deletePost(UUID postId, UUID userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> BoondiException.postNotFound(postId.toString()));

        if (!post.getAuthor().getId().equals(userId)) {
            throw BoondiException.postAccessDenied();
        }

        softDeletePost(post);
    }

    /** Admin moderation delete (E9-03) — same soft-delete/counter rollback, no ownership check. */
    @Transactional
    public void adminDeletePost(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> BoondiException.postNotFound(postId.toString()));

        softDeletePost(post);
        log.info("Post admin-deleted: postId={}", postId);
    }

    private void softDeletePost(Post post) {
        UUID postId = post.getId();
        post.setDeletedAt(OffsetDateTime.now());
        postRepository.save(post);

        User author = post.getAuthor();
        author.setPostCount(Math.max(0, author.getPostCount() - 1));
        userRepository.save(author);

        // Roll back the counters this post contributed to (parent may itself be deleted —
        // its lazy proxy then fails to resolve under @SQLRestriction, which we can ignore)
        try {
            Post parent = post.getParentPost();
            if (parent != null) {
                parent.setReplyCount(Math.max(0, parent.getReplyCount() - 1));
                postRepository.save(parent);
            }
        } catch (jakarta.persistence.EntityNotFoundException ignored) {
        }
        try {
            Post quoted = post.getQuotedPost();
            if (quoted != null) {
                quoted.setRepostCount(Math.max(0, quoted.getRepostCount() - 1));
                postRepository.save(quoted);
            }
        } catch (jakarta.persistence.EntityNotFoundException ignored) {
        }

        timelineCacheService.evictFollowersOf(author.getId());

        log.info("Post soft-deleted: postId={}", postId);
    }

    @Transactional
    public UploadResponse uploadPostImage(UUID userId, MultipartFile file) {
        validatePostImage(file);

        String extension = extensionFromContentType(file.getContentType());
        String key = "posts/" + userId + "/" + UUID.randomUUID() + "." + extension;

        try {
            String url = storageService.uploadFile(key, file.getInputStream(),
                    file.getSize(), file.getContentType());
            log.info("Post image uploaded by userId={}: {}", userId, url);
            return UploadResponse.of(url);
        } catch (BoondiException e) {
            throw e;
        } catch (Exception e) {
            throw BoondiException.fileUploadFailed(e.getMessage());
        }
    }

    private void validatePostImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw BoondiException.fileUploadFailed("No file provided");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw BoondiException.invalidFileType();
        }
        if (file.getSize() > MAX_POST_IMAGE_SIZE) {
            throw BoondiException.fileTooLarge(MAX_POST_IMAGE_SIZE);
        }
    }

    private String extensionFromContentType(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png"  -> "png";
            case "image/webp" -> "webp";
            default           -> "jpg";
        };
    }
}
