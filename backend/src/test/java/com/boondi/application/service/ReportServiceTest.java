package com.boondi.application.service;

import com.boondi.application.dto.request.CreateReportRequest;
import com.boondi.application.mapper.ReportMapper;
import com.boondi.domain.entity.Post;
import com.boondi.domain.entity.Report;
import com.boondi.domain.entity.User;
import com.boondi.domain.repository.PostRepository;
import com.boondi.domain.repository.ReportRepository;
import com.boondi.domain.repository.UserRepository;
import com.boondi.infrastructure.exception.BoondiException;
import com.boondi.infrastructure.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private ReportRepository reportRepository;
    @Mock private UserRepository userRepository;
    @Mock private PostRepository postRepository;
    @Mock private ReportMapper reportMapper;

    @InjectMocks private ReportService reportService;

    private UUID reporterId;
    private User reporter;

    @BeforeEach
    void setUp() {
        reporterId = UUID.randomUUID();
        reporter = User.builder().username("reporter").build();
        reporter.setId(reporterId);
        lenient().when(userRepository.findById(reporterId)).thenReturn(Optional.of(reporter));
        lenient().when(reportRepository.saveAndFlush(any(Report.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private CreateReportRequest request(UUID reportedUserId, UUID reportedPostId) {
        CreateReportRequest request = new CreateReportRequest();
        request.setReason("  spam  ");
        request.setReportedUserId(reportedUserId);
        request.setReportedPostId(reportedPostId);
        return request;
    }

    @Test
    void createReport_rejectsWhenNoTargetSet() {
        assertThatThrownBy(() -> reportService.createReport(reporterId, request(null, null)))
                .isInstanceOfSatisfying(BoondiException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_REPORT_TARGET));
    }

    @Test
    void createReport_rejectsWhenBothTargetsSet() {
        assertThatThrownBy(() -> reportService.createReport(reporterId,
                request(UUID.randomUUID(), UUID.randomUUID())))
                .isInstanceOfSatisfying(BoondiException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_REPORT_TARGET));
        verify(reportRepository, never()).saveAndFlush(any());
    }

    @Test
    void createReport_rejectsReportingYourself() {
        assertThatThrownBy(() -> reportService.createReport(reporterId, request(reporterId, null)))
                .isInstanceOfSatisfying(BoondiException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.CANNOT_REPORT_SELF));
    }

    @Test
    void createReport_rejectsReportingYourOwnPost() {
        Post ownPost = Post.builder().author(reporter).content("mine").build();
        UUID postId = UUID.randomUUID();
        ownPost.setId(postId);
        when(postRepository.findById(postId)).thenReturn(Optional.of(ownPost));

        assertThatThrownBy(() -> reportService.createReport(reporterId, request(null, postId)))
                .isInstanceOfSatisfying(BoondiException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.CANNOT_REPORT_SELF));
    }

    @Test
    void createReport_userTarget_savesTrimmedReason() {
        UUID reportedId = UUID.randomUUID();
        User reported = User.builder().username("baddie").build();
        reported.setId(reportedId);
        when(userRepository.findById(reportedId)).thenReturn(Optional.of(reported));

        reportService.createReport(reporterId, request(reportedId, null));

        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository).saveAndFlush(captor.capture());
        Report saved = captor.getValue();
        assertThat(saved.getReason()).isEqualTo("spam");
        assertThat(saved.getReportedUser()).isEqualTo(reported);
        assertThat(saved.getReportedPost()).isNull();
    }

    @Test
    void createReport_postTarget_resolvesPost() {
        User other = User.builder().username("other").build();
        other.setId(UUID.randomUUID());
        Post post = Post.builder().author(other).content("bad post").build();
        UUID postId = UUID.randomUUID();
        post.setId(postId);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        reportService.createReport(reporterId, request(null, postId));

        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getReportedPost()).isEqualTo(post);
        assertThat(captor.getValue().getReportedUser()).isNull();
    }
}
