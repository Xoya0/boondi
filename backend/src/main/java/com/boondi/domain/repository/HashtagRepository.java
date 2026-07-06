package com.boondi.domain.repository;

import com.boondi.domain.entity.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HashtagRepository extends JpaRepository<Hashtag, java.util.UUID> {

    Optional<Hashtag> findByTag(String tag);

    // Prefix search, case-insensitive (tags are stored lowercase, but the query might not be),
    // with real usage counts (non-deleted posts only) — same shape as PostHashtagRepository's
    // trending query: row = [tag (String), postCount (Long)].
    @Query(value = "SELECT h.tag, COUNT(ph.post_id) FILTER (WHERE p.deleted_at IS NULL) AS post_count " +
                   "FROM hashtags h " +
                   "LEFT JOIN post_hashtags ph ON ph.hashtag_id = h.id " +
                   "LEFT JOIN posts p ON p.id = ph.post_id " +
                   "WHERE h.tag ILIKE CONCAT(:query, '%') " +
                   "GROUP BY h.tag " +
                   "ORDER BY h.tag ASC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Object[]> searchByPrefixWithCount(@Param("query") String query,
                                           @Param("limit") int limit,
                                           @Param("offset") int offset);
}
