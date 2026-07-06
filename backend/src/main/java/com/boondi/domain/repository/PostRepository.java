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

    // Full-text search (E8-02) against the generated `search_vector` tsvector column.
    // Ranked by relevance, then recency. Column list is explicit so Hibernate's entity
    // result mapping doesn't choke on the unmapped search_vector column from SELECT *.
    @Query(value =
            "SELECT p.id, p.author_id, p.content, p.image_url, p.like_count, p.repost_count, " +
            "p.reply_count, p.bookmark_count, p.parent_post_id, p.quoted_post_id, " +
            "p.is_edited, p.edited_at, p.created_at, p.updated_at, p.deleted_at " +
            "FROM posts p " +
            "WHERE p.deleted_at IS NULL AND p.search_vector @@ plainto_tsquery('english', :query) " +
            "ORDER BY ts_rank(p.search_vector, plainto_tsquery('english', :query)) DESC, p.created_at DESC " +
            "LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<Post> searchPosts(@Param("query") String query,
                           @Param("limit") int limit,
                           @Param("offset") int offset);

    // Bookmarked posts for a user, most recently bookmarked first (E6-14).
    // Row shape: [Post, bookmark.createdAt] — the bookmark timestamp (not the post's
    // own createdAt) is the natural sort key and cursor for "my bookmarks".
    @Query("SELECT p, b.createdAt FROM PostBookmark b " +
           "JOIN Post p ON p.id = b.postId " +
           "JOIN FETCH p.author " +
           "LEFT JOIN FETCH p.quotedPost q LEFT JOIN FETCH q.author " +
           "WHERE b.userId = :userId " +
           "AND (:cursor IS NULL OR b.createdAt < :cursor) " +
           "ORDER BY b.createdAt DESC")
    List<Object[]> findBookmarkedPosts(@Param("userId") UUID userId,
                                       @Param("cursor") OffsetDateTime cursor,
                                       Pageable pageable);
}
