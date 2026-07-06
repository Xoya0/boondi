package com.boondi.domain.repository;

import com.boondi.domain.entity.PostBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PostBookmarkRepository extends JpaRepository<PostBookmark, PostBookmark.PostBookmarkId> {

    boolean existsByUserIdAndPostId(UUID userId, UUID postId);

    void deleteByUserIdAndPostId(UUID userId, UUID postId);

    @Query("SELECT b.postId FROM PostBookmark b WHERE b.userId = :userId AND b.postId IN :postIds")
    List<UUID> findBookmarkedPostIds(@Param("userId") UUID userId, @Param("postIds") Collection<UUID> postIds);
}
