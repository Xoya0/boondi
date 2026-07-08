package com.boondi.application.service;

import com.boondi.application.dto.response.PostResponse;
import com.boondi.application.mapper.PostMapper;
import com.boondi.domain.entity.Post;
import com.boondi.domain.entity.User;
import com.boondi.domain.repository.PostBookmarkRepository;
import com.boondi.domain.repository.PostLikeRepository;
import com.boondi.domain.repository.PostRepository;
import com.boondi.domain.repository.PostRepostRepository;
import com.boondi.infrastructure.exception.BoondiException;
import com.boondi.infrastructure.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InteractionServiceTest {

    @Mock private PostRepository postRepository;
    @Mock private PostLikeRepository postLikeRepository;
    @Mock private PostRepostRepository postRepostRepository;
    @Mock private PostBookmarkRepository postBookmarkRepository;
    @Mock private PostMapper postMapper;
    @Mock private PostViewerStateService postViewerStateService;
    @Mock private NotificationService notificationService;

    @InjectMocks private InteractionService interactionService;

    private final UUID userId = UUID.randomUUID();
    private Post post;
    private UUID postId;

    @BeforeEach
    void setUp() {
        User author = User.builder().username("author").build();
        author.setId(UUID.randomUUID());
        post = Post.builder().author(author).content("hello").build();
        postId = UUID.randomUUID();
        post.setId(postId);
        lenient().when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        lenient().when(postMapper.toResponse(any(Post.class))).thenReturn(mock(PostResponse.class));
    }

    @Test
    void like_rejectsSoftDeletedPost() {
        post.setDeletedAt(OffsetDateTime.now());

        assertThatThrownBy(() -> interactionService.like(userId, postId))
                .isInstanceOfSatisfying(BoondiException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.POST_NOT_FOUND));
    }

    @Test
    void like_rejectsDuplicate() {
        when(postLikeRepository.existsByUserIdAndPostId(userId, postId)).thenReturn(true);

        assertThatThrownBy(() -> interactionService.like(userId, postId))
                .isInstanceOfSatisfying(BoondiException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.ALREADY_LIKED));
        assertThat(post.getLikeCount()).isZero();
    }

    @Test
    void like_incrementsCounter_andNotifies() {
        when(postLikeRepository.existsByUserIdAndPostId(userId, postId)).thenReturn(false);

        interactionService.like(userId, postId);

        assertThat(post.getLikeCount()).isEqualTo(1);
        verify(postLikeRepository).save(any());
        verify(notificationService).notifyLike(userId, post);
    }

    @Test
    void unlike_rejectsWhenNotLiked() {
        when(postLikeRepository.existsByUserIdAndPostId(userId, postId)).thenReturn(false);

        assertThatThrownBy(() -> interactionService.unlike(userId, postId))
                .isInstanceOfSatisfying(BoondiException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.NOT_LIKED));
    }

    @Test
    void unlike_floorsCounterAtZero() {
        // Counter drift (e.g. a missed decrement elsewhere) must never push it negative.
        post.setLikeCount(0);
        when(postLikeRepository.existsByUserIdAndPostId(userId, postId)).thenReturn(true);

        interactionService.unlike(userId, postId);

        assertThat(post.getLikeCount()).isZero();
        verify(postLikeRepository).deleteByUserIdAndPostId(userId, postId);
    }

    @Test
    void repost_rejectsDuplicate() {
        when(postRepostRepository.existsByUserIdAndPostId(userId, postId)).thenReturn(true);

        assertThatThrownBy(() -> interactionService.repost(userId, postId))
                .isInstanceOfSatisfying(BoondiException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.ALREADY_REPOSTED));
    }

    @Test
    void repost_incrementsCounter_andNotifies() {
        when(postRepostRepository.existsByUserIdAndPostId(userId, postId)).thenReturn(false);

        interactionService.repost(userId, postId);

        assertThat(post.getRepostCount()).isEqualTo(1);
        verify(notificationService).notifyRepost(userId, post);
    }

    @Test
    void bookmark_rejectsDuplicate() {
        when(postBookmarkRepository.existsByUserIdAndPostId(userId, postId)).thenReturn(true);

        assertThatThrownBy(() -> interactionService.bookmark(userId, postId))
                .isInstanceOfSatisfying(BoondiException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.ALREADY_BOOKMARKED));
    }

    @Test
    void bookmark_incrementsCounter_withoutNotification() {
        when(postBookmarkRepository.existsByUserIdAndPostId(userId, postId)).thenReturn(false);

        interactionService.bookmark(userId, postId);

        assertThat(post.getBookmarkCount()).isEqualTo(1);
        // Bookmarks are private — the post author must not be notified.
        verify(notificationService, org.mockito.Mockito.never()).notifyLike(any(), any());
        verify(notificationService, org.mockito.Mockito.never()).notifyRepost(any(), any());
    }

    @Test
    void unbookmark_rejectsWhenNotBookmarked() {
        when(postBookmarkRepository.existsByUserIdAndPostId(userId, postId)).thenReturn(false);

        assertThatThrownBy(() -> interactionService.unbookmark(userId, postId))
                .isInstanceOfSatisfying(BoondiException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.NOT_BOOKMARKED));
    }
}
