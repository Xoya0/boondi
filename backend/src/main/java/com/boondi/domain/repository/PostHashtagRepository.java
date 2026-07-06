package com.boondi.domain.repository;

import com.boondi.domain.entity.PostHashtag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface PostHashtagRepository extends JpaRepository<PostHashtag, PostHashtag.PostHashtagId> {

    // Top hashtags by usage count on non-deleted posts created after :since.
    // Row shape: [tag (String), useCount (Long)]
    @Query("SELECT h.tag, COUNT(ph.postId) FROM PostHashtag ph JOIN Hashtag h ON h.id = ph.hashtagId " +
           "WHERE ph.postId IN (SELECT p.id FROM Post p WHERE p.createdAt > :since) " +
           "GROUP BY h.tag ORDER BY COUNT(ph.postId) DESC")
    List<Object[]> findTrendingHashtags(@Param("since") OffsetDateTime since, Pageable pageable);
}
