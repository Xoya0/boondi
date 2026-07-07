package com.boondi.domain.repository;

import com.boondi.domain.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query("SELECT n FROM Notification n JOIN FETCH n.actor " +
           "LEFT JOIN FETCH n.post p LEFT JOIN FETCH p.author " +
           "WHERE n.recipient.id = :recipientId " +
           "AND (cast(:cursor as timestamp) IS NULL OR n.createdAt < :cursor) " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findByRecipient(@Param("recipientId") UUID recipientId,
                                        @Param("cursor") OffsetDateTime cursor,
                                        Pageable pageable);

    long countByRecipientIdAndReadFalse(UUID recipientId);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.recipient.id = :recipientId AND n.read = false")
    void markAllAsRead(@Param("recipientId") UUID recipientId);
}
