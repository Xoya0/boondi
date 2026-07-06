package com.boondi.application.service;

import com.boondi.application.dto.response.PostResponse;
import com.boondi.application.mapper.PostMapper;
import com.boondi.domain.entity.Post;
import com.boondi.domain.entity.PostBookmark;
import com.boondi.domain.entity.PostLike;
import com.boondi.domain.entity.PostRepost;
import com.boondi.domain.repository.PostBookmarkRepository;
import com.boondi.domain.repository.PostLikeRepository;
import com.boondi.domain.repository.PostRepository;
import com.boondi.domain.repository.PostRepostRepository;
import com.boondi.infrastructure.exception.BoondiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Like / Repost / Bookmark actions (E6-01, E6-02, E6-04).
 * Each action inserts/deletes a (user, post) row and adjusts the counter on Post.
 * Returns the updated post with viewer flags so clients can sync state.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InteractionService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostRepostRepository postRepostRepository;
    private final PostBookmarkRepository postBookmarkRepository;
    private final PostMapper postMapper;
    private final PostViewerStateService postViewerStateService;

    @Transactional
    public PostResponse like(UUID userId, UUID postId) {
        Post post = loadPost(postId);
        if (postLikeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw BoondiException.alreadyLiked();
        }
        postLikeRepository.save(PostLike.builder().userId(userId).postId(postId).build());
        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);
        log.info("Post liked: postId={}, userId={}", postId, userId);
        return toEnrichedResponse(post, userId);
    }

    @Transactional
    public PostResponse unlike(UUID userId, UUID postId) {
        Post post = loadPost(postId);
        if (!postLikeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw BoondiException.notLiked();
        }
        postLikeRepository.deleteByUserIdAndPostId(userId, postId);
        post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        postRepository.save(post);
        log.info("Post unliked: postId={}, userId={}", postId, userId);
        return toEnrichedResponse(post, userId);
    }

    @Transactional
    public PostResponse repost(UUID userId, UUID postId) {
        Post post = loadPost(postId);
        if (postRepostRepository.existsByUserIdAndPostId(userId, postId)) {
            throw BoondiException.alreadyReposted();
        }
        postRepostRepository.save(PostRepost.builder().userId(userId).postId(postId).build());
        post.setRepostCount(post.getRepostCount() + 1);
        postRepository.save(post);
        log.info("Post reposted: postId={}, userId={}", postId, userId);
        return toEnrichedResponse(post, userId);
    }

    @Transactional
    public PostResponse unrepost(UUID userId, UUID postId) {
        Post post = loadPost(postId);
        if (!postRepostRepository.existsByUserIdAndPostId(userId, postId)) {
            throw BoondiException.notReposted();
        }
        postRepostRepository.deleteByUserIdAndPostId(userId, postId);
        post.setRepostCount(Math.max(0, post.getRepostCount() - 1));
        postRepository.save(post);
        log.info("Post unreposted: postId={}, userId={}", postId, userId);
        return toEnrichedResponse(post, userId);
    }

    @Transactional
    public PostResponse bookmark(UUID userId, UUID postId) {
        Post post = loadPost(postId);
        if (postBookmarkRepository.existsByUserIdAndPostId(userId, postId)) {
            throw BoondiException.alreadyBookmarked();
        }
        postBookmarkRepository.save(PostBookmark.builder().userId(userId).postId(postId).build());
        post.setBookmarkCount(post.getBookmarkCount() + 1);
        postRepository.save(post);
        log.info("Post bookmarked: postId={}, userId={}", postId, userId);
        return toEnrichedResponse(post, userId);
    }

    @Transactional
    public PostResponse unbookmark(UUID userId, UUID postId) {
        Post post = loadPost(postId);
        if (!postBookmarkRepository.existsByUserIdAndPostId(userId, postId)) {
            throw BoondiException.notBookmarked();
        }
        postBookmarkRepository.deleteByUserIdAndPostId(userId, postId);
        post.setBookmarkCount(Math.max(0, post.getBookmarkCount() - 1));
        postRepository.save(post);
        log.info("Post unbookmarked: postId={}, userId={}", postId, userId);
        return toEnrichedResponse(post, userId);
    }

    private Post loadPost(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> BoondiException.postNotFound(postId.toString()));
    }

    private PostResponse toEnrichedResponse(Post post, UUID viewerId) {
        PostResponse response = postMapper.toResponse(post);
        postViewerStateService.enrich(viewerId, response);
        return response;
    }
}
