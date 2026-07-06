package com.boondi.domain.repository;

import com.boondi.domain.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {

    // Latest timeline — all posts reverse-chronological (public)
    @Query("SELECT p FROM Post p JOIN FETCH p.author " +
           "LEFT JOIN FETCH p.quotedPost q LEFT JOIN FETCH q.author " +
           "WHERE (:cursor IS NULL OR p.createdAt < :cursor) " +
           "ORDER BY p.createdAt DESC")
    List<Post> findLatestTimeline(@Param("cursor") OffsetDateTime cursor, Pageable pageable);

    // Home timeline — posts from users the authenticated user follows
    @Query("SELECT p FROM Post p JOIN FETCH p.author " +
           "LEFT JOIN FETCH p.quotedPost q LEFT JOIN FETCH q.author " +
           "WHERE p.author.id IN (SELECT f.followeeId FROM Follow f WHERE f.followerId = :userId) " +
           "AND (:cursor IS NULL OR p.createdAt < :cursor) " +
           "ORDER BY p.createdAt DESC")
    List<Post> findHomeTimeline(@Param("userId") UUID userId,
                                @Param("cursor") OffsetDateTime cursor,
                                Pageable pageable);

    // User timeline — posts by a specific user
    @Query("SELECT p FROM Post p JOIN FETCH p.author " +
           "LEFT JOIN FETCH p.quotedPost q LEFT JOIN FETCH q.author " +
           "WHERE p.author.username = :username " +
           "AND (:cursor IS NULL OR p.createdAt < :cursor) " +
           "ORDER BY p.createdAt DESC")
    List<Post> findUserTimeline(@Param("username") String username,
                                @Param("cursor") OffsetDateTime cursor,
                                Pageable pageable);

    // Trending timeline — posts from the last 24h scored by (likes × 1) + (reposts × 2).
    // Offset-paginated internally; the service exposes it through the CursorPage shape.
    @Query("SELECT p FROM Post p JOIN FETCH p.author " +
           "LEFT JOIN FETCH p.quotedPost q LEFT JOIN FETCH q.author " +
           "WHERE p.createdAt > :since " +
           "ORDER BY (p.likeCount + p.repostCount * 2) DESC, p.createdAt DESC")
    List<Post> findTrending(@Param("since") OffsetDateTime since, Pageable pageable);

    // Replies to a post, oldest first (conversation order)
    @Query("SELECT p FROM Post p JOIN FETCH p.author " +
           "WHERE p.parentPost.id = :parentPostId " +
           "AND (:cursor IS NULL OR p.createdAt > :cursor) " +
           "ORDER BY p.createdAt ASC")
    List<Post> findReplies(@Param("parentPostId") UUID parentPostId,
                           @Param("cursor") OffsetDateTime cursor,
                           Pageable pageable);
}
