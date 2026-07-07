package com.boondi.domain.repository;

import com.boondi.domain.entity.Report;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {

    @Query("SELECT r FROM Report r JOIN FETCH r.reporter " +
           "LEFT JOIN FETCH r.reportedUser " +
           "LEFT JOIN FETCH r.reportedPost p LEFT JOIN FETCH p.author " +
           "WHERE (cast(:cursor as timestamp) IS NULL OR r.createdAt < :cursor) " +
           "ORDER BY r.createdAt DESC")
    List<Report> findAllForAdmin(@Param("cursor") OffsetDateTime cursor, Pageable pageable);
}
