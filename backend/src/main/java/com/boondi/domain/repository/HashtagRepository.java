package com.boondi.domain.repository;

import com.boondi.domain.entity.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HashtagRepository extends JpaRepository<Hashtag, java.util.UUID> {

    Optional<Hashtag> findByTag(String tag);

    // Prefix search, case-insensitive (tags are stored lowercase, but the query might not be)
    @Query(value = "SELECT * FROM hashtags h WHERE h.tag ILIKE CONCAT(:query, '%') " +
                   "ORDER BY h.tag ASC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Hashtag> searchByPrefix(@Param("query") String query,
                                 @Param("limit") int limit,
                                 @Param("offset") int offset);
}
