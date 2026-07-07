package com.boondi.application.mapper;

import com.boondi.application.dto.response.ReportResponse;
import com.boondi.domain.entity.Post;
import com.boondi.domain.entity.Report;
import com.boondi.domain.entity.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class ReportMapper {

    private static final int PREVIEW_LENGTH = 80;

    public ReportResponse toResponse(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .reporter(toReporterInfo(report.getReporter()))
                .reportedUser(toReportedUserInfo(report))
                .reportedPost(toReportedPostInfo(report))
                .reason(report.getReason())
                .createdAt(report.getCreatedAt())
                .build();
    }

    private ReportResponse.ReporterInfo toReporterInfo(User user) {
        return ReportResponse.ReporterInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .build();
    }

    // The reported user/post may have been soft-deleted since the report was filed, which
    // makes the lazy proxy throw EntityNotFoundException under @SQLRestriction — degrade to null.
    private ReportResponse.ReportedUserInfo toReportedUserInfo(Report report) {
        try {
            User user = report.getReportedUser();
            if (user == null) return null;
            return ReportResponse.ReportedUserInfo.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .displayName(user.getDisplayName())
                    .build();
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    private ReportResponse.ReportedPostInfo toReportedPostInfo(Report report) {
        try {
            Post post = report.getReportedPost();
            if (post == null) return null;
            String content = post.getContent();
            String preview = content.length() > PREVIEW_LENGTH
                    ? content.substring(0, PREVIEW_LENGTH) + "…"
                    : content;
            return ReportResponse.ReportedPostInfo.builder()
                    .id(post.getId())
                    .contentPreview(preview)
                    .authorUsername(post.getAuthor().getUsername())
                    .build();
        } catch (EntityNotFoundException e) {
            return null;
        }
    }
}
