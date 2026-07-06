package com.boondi.application.service;

import com.boondi.application.dto.response.PostResponse;
import com.boondi.domain.repository.PostBookmarkRepository;
import com.boondi.domain.repository.PostLikeRepository;
import com.boondi.domain.repository.PostRepostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Fills the per-viewer flags (likedByViewer / repostedByViewer / bookmarkedByViewer)
 * on already-mapped PostResponses. Uses one batch query per interaction type.
 * No-op for anonymous viewers — flags stay false.
 */
@Service
@RequiredArgsConstructor
public class PostViewerStateService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepostRepository postRepostRepository;
    private final PostBookmarkRepository postBookmarkRepository;

    @Transactional(readOnly = true)
    public void enrich(UUID viewerId, List<PostResponse> posts) {
        if (viewerId == null || posts == null || posts.isEmpty()) return;

        List<UUID> postIds = posts.stream().map(PostResponse::getId).toList();

        Set<UUID> liked = new HashSet<>(postLikeRepository.findLikedPostIds(viewerId, postIds));
        Set<UUID> reposted = new HashSet<>(postRepostRepository.findRepostedPostIds(viewerId, postIds));
        Set<UUID> bookmarked = new HashSet<>(postBookmarkRepository.findBookmarkedPostIds(viewerId, postIds));

        for (PostResponse post : posts) {
            post.setLikedByViewer(liked.contains(post.getId()));
            post.setRepostedByViewer(reposted.contains(post.getId()));
            post.setBookmarkedByViewer(bookmarked.contains(post.getId()));
        }
    }

    @Transactional(readOnly = true)
    public void enrich(UUID viewerId, PostResponse post) {
        if (post != null) enrich(viewerId, List.of(post));
    }
}
