package com.boondi.application.service;

import com.boondi.application.dto.request.CreatePostRequest;
import com.boondi.application.dto.request.UpdatePostRequest;
import com.boondi.application.dto.response.PostResponse;
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
import com.boondi.infrastructure.exception.ErrorCode;
import com.boondi.infrastructure.service.ImageContentValidator;
import com.boondi.infrastructure.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock private PostRepository postRepository;
    @Mock private UserRepository userRepository;
    @Mock private PostMapper postMapper;
    @Mock private StorageService storageService;
    @Mock private PostViewerStateService postViewerStateService;
    @Mock private TimelineCacheService timelineCacheService;
    @Mock private NotificationService notificationService;
    @Mock private HashtagRepository hashtagRepository;
    @Mock private PostHashtagRepository postHashtagRepository;
    @Mock private ImageContentValidator imageContentValidator;

    @InjectMocks private PostService postService;

    private User author;
    private UUID authorId;

    @BeforeEach
    void setUp() {
        authorId = UUID.randomUUID();
        author = User.builder().username("john").build();
        author.setId(authorId);
        lenient().when(postMapper.toResponse(any(Post.class))).thenReturn(mock(PostResponse.class));
        lenient().when(postRepository.saveAndFlush(any(Post.class))).thenAnswer(inv -> {
            Post p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });
    }

    private CreatePostRequest createRequest(String content) {
        CreatePostRequest request = new CreatePostRequest();
        request.setContent(content);
        return request;
    }

    @Test
    void createPost_rejectsUnknownAuthor() {
        when(userRepository.findById(authorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.createPost(authorId, createRequest("hi")))
                .isInstanceOfSatisfying(BoondiException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    void createPost_incrementsAuthorPostCount_andEvictsFollowerCaches() {
        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));

        postService.createPost(authorId, createRequest("hello world"));

        assertThat(author.getPostCount()).isEqualTo(1);
        verify(timelineCacheService).evictFollowersOf(authorId);
    }

    @Test
    void createPost_rejectsSoftDeletedParent() {
        Post parent = Post.builder().author(author).content("parent").build();
        parent.setDeletedAt(OffsetDateTime.now());
        UUID parentId = UUID.randomUUID();
        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(postRepository.findById(parentId)).thenReturn(Optional.of(parent));

        CreatePostRequest request = createRequest("reply");
        request.setParentPostId(parentId);

        assertThatThrownBy(() -> postService.createPost(authorId, request))
                .isInstanceOfSatisfying(BoondiException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.POST_NOT_FOUND));
    }

    @Test
    void createReply_incrementsParentReplyCount_andNotifiesParentAuthor() {
        Post parent = Post.builder().author(author).content("parent").build();
        UUID parentId = UUID.randomUUID();
        parent.setId(parentId);
        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(postRepository.findById(parentId)).thenReturn(Optional.of(parent));

        CreatePostRequest request = createRequest("a reply");
        request.setParentPostId(parentId);
        postService.createPost(authorId, request);

        assertThat(parent.getReplyCount()).isEqualTo(1);
        verify(notificationService).notifyReply(authorId, parent);
    }

    @Test
    void createQuote_incrementsQuotedRepostCount_andNotifiesQuotedAuthor() {
        Post quoted = Post.builder().author(author).content("original").build();
        UUID quotedId = UUID.randomUUID();
        quoted.setId(quotedId);
        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(postRepository.findById(quotedId)).thenReturn(Optional.of(quoted));

        CreatePostRequest request = createRequest("look at this");
        request.setQuotedPostId(quotedId);
        postService.createPost(authorId, request);

        assertThat(quoted.getRepostCount()).isEqualTo(1);
        verify(notificationService).notifyRepost(authorId, quoted);
    }

    @Test
    void createPost_extractsHashtags_dedupedCaseInsensitive_storedLowercase() {
        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(hashtagRepository.findByTag("java")).thenReturn(Optional.empty());
        when(hashtagRepository.save(any(Hashtag.class))).thenAnswer(inv -> {
            Hashtag h = inv.getArgument(0);
            h.setId(UUID.randomUUID());
            return h;
        });

        postService.createPost(authorId, createRequest("Loving #Java and #JAVA and #java"));

        ArgumentCaptor<Hashtag> tagCaptor = ArgumentCaptor.forClass(Hashtag.class);
        verify(hashtagRepository, times(1)).save(tagCaptor.capture());
        assertThat(tagCaptor.getValue().getTag()).isEqualTo("java");
        verify(postHashtagRepository, times(1)).save(any(PostHashtag.class));
    }

    @Test
    void updatePost_rejectsNonAuthor() {
        Post post = ownPost(OffsetDateTime.now());
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        UUID stranger = UUID.randomUUID();
        assertThatThrownBy(() -> postService.updatePost(post.getId(), stranger, updateRequest("x")))
                .isInstanceOfSatisfying(BoondiException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.POST_ACCESS_DENIED));
    }

    @Test
    void updatePost_rejectsAfterEditWindow() {
        Post post = ownPost(OffsetDateTime.now().minusMinutes(31));
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.updatePost(post.getId(), authorId, updateRequest("x")))
                .isInstanceOfSatisfying(BoondiException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.POST_EDIT_WINDOW_EXPIRED));
    }

    @Test
    void updatePost_withinWindow_marksEdited() {
        Post post = ownPost(OffsetDateTime.now().minusMinutes(5));
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        postService.updatePost(post.getId(), authorId, updateRequest("new content"));

        assertThat(post.getContent()).isEqualTo("new content");
        assertThat(post.isEdited()).isTrue();
        assertThat(post.getEditedAt()).isNotNull();
    }

    @Test
    void deletePost_rejectsNonAuthor() {
        Post post = ownPost(OffsetDateTime.now());
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.deletePost(post.getId(), UUID.randomUUID()))
                .isInstanceOfSatisfying(BoondiException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.POST_ACCESS_DENIED));
        assertThat(post.getDeletedAt()).isNull();
    }

    @Test
    void deletePost_softDeletes_andDecrementsAuthorPostCountWithFloorAtZero() {
        Post post = ownPost(OffsetDateTime.now());
        author.setPostCount(0); // floor case: counter must not go negative
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        postService.deletePost(post.getId(), authorId);

        assertThat(post.getDeletedAt()).isNotNull();
        assertThat(author.getPostCount()).isZero();
        verify(timelineCacheService).evictFollowersOf(authorId);
    }

    @Test
    void adminDeletePost_skipsOwnershipCheck() {
        Post post = ownPost(OffsetDateTime.now());
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        postService.adminDeletePost(post.getId());

        assertThat(post.getDeletedAt()).isNotNull();
    }

    @Test
    void uploadPostImage_rejectsEmptyFile() {
        MockMultipartFile empty = new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> postService.uploadPostImage(authorId, empty))
                .isInstanceOfSatisfying(BoondiException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.FILE_UPLOAD_FAILED));
        verify(storageService, never()).uploadFile(any(), any(), anyLong(), any());
    }

    @Test
    void uploadPostImage_rejectsDisallowedContentType() {
        MockMultipartFile svg = new MockMultipartFile("file", "a.svg", "image/svg+xml", new byte[]{1});

        // SVG can carry scripts — only jpeg/png/webp are allowed.
        assertThatThrownBy(() -> postService.uploadPostImage(authorId, svg))
                .isInstanceOfSatisfying(BoondiException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_FILE_TYPE));
    }

    @Test
    void uploadPostImage_rejectsOversizedFile() {
        lenient().when(imageContentValidator.matches(any(), any())).thenReturn(true);
        byte[] sixMb = new byte[6 * 1024 * 1024];
        MockMultipartFile big = new MockMultipartFile("file", "a.jpg", "image/jpeg", sixMb);

        assertThatThrownBy(() -> postService.uploadPostImage(authorId, big))
                .isInstanceOfSatisfying(BoondiException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.FILE_TOO_LARGE));
    }

    private Post ownPost(OffsetDateTime createdAt) {
        Post post = Post.builder().author(author).content("original").build();
        post.setId(UUID.randomUUID());
        post.setCreatedAt(createdAt);
        return post;
    }

    private UpdatePostRequest updateRequest(String content) {
        UpdatePostRequest request = new UpdatePostRequest();
        request.setContent(content);
        return request;
    }
}
