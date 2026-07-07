package com.boondi.application.service;

import com.boondi.application.dto.request.CreateReportRequest;
import com.boondi.application.dto.response.CursorPage;
import com.boondi.application.dto.response.ReportResponse;
import com.boondi.application.mapper.ReportMapper;
import com.boondi.domain.entity.Post;
import com.boondi.domain.entity.Report;
import com.boondi.domain.entity.User;
import com.boondi.domain.repository.PostRepository;
import com.boondi.domain.repository.ReportRepository;
import com.boondi.domain.repository.UserRepository;
import com.boondi.infrastructure.exception.BoondiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * User-facing report creation (E9-05) + admin report listing (E9-04).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private static final int MAX_LIMIT = 50;

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ReportMapper reportMapper;

    @Transactional
    public ReportResponse createReport(UUID reporterId, CreateReportRequest request) {
        boolean hasUserTarget = request.getReportedUserId() != null;
        boolean hasPostTarget = request.getReportedPostId() != null;
        if (hasUserTarget == hasPostTarget) { // both or neither set
            throw BoondiException.invalidReportTarget();
        }

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> BoondiException.userNotFound(reporterId.toString()));

        User reportedUser = null;
        Post reportedPost = null;

        if (hasUserTarget) {
            if (request.getReportedUserId().equals(reporterId)) {
                throw BoondiException.cannotReportSelf();
            }
            reportedUser = userRepository.findById(request.getReportedUserId())
                    .orElseThrow(() -> BoondiException.userNotFound(request.getReportedUserId().toString()));
        } else {
            reportedPost = postRepository.findById(request.getReportedPostId())
                    .orElseThrow(() -> BoondiException.postNotFound(request.getReportedPostId().toString()));
            if (reportedPost.getAuthor().getId().equals(reporterId)) {
                throw BoondiException.cannotReportSelf();
            }
        }

        Report report = Report.builder()
                .reporter(reporter)
                .reportedUser(reportedUser)
                .reportedPost(reportedPost)
                .reason(request.getReason().trim())
                .build();

        Report saved = reportRepository.save(report);
        log.info("Report created: reportId={}, reporterId={}", saved.getId(), reporterId);
        return reportMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CursorPage<ReportResponse> getReports(String cursorStr, int limit) {
        int pageSize = clampLimit(limit);
        OffsetDateTime cursor = parseCursor(cursorStr);

        List<Report> raw = reportRepository.findAllForAdmin(cursor, PageRequest.of(0, pageSize + 1));
        boolean hasMore = raw.size() > pageSize;
        List<Report> page = hasMore ? raw.subList(0, pageSize) : raw;

        List<ReportResponse> items = page.stream().map(reportMapper::toResponse).toList();
        String nextCursor = (hasMore && !page.isEmpty())
                ? page.get(page.size() - 1).getCreatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                : null;

        return CursorPage.<ReportResponse>builder()
                .items(items)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .count(items.size())
                .build();
    }

    private int clampLimit(int requested) {
        return Math.min(Math.max(requested, 1), MAX_LIMIT);
    }

    private OffsetDateTime parseCursor(String cursorStr) {
        if (cursorStr == null || cursorStr.isBlank()) return null;
        try {
            return OffsetDateTime.parse(cursorStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (Exception e) {
            log.warn("Invalid report cursor '{}' — treating as first page", cursorStr);
            return null;
        }
    }
}
