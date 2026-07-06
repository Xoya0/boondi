package com.boondi.domain.repository;

import com.boondi.domain.entity.PostRepost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PostRepostRepository extends JpaRepository<PostRepost, PostRepost.PostRepostId> {

    boolean existsByUserIdAndPostId(UUID userId, UUID postId);

    void deleteByUserIdAndPostId(UUID userId, UUID postId);

    @Query("SELECT r.postId FROM PostRepost r WHERE r.userId = :userId AND r.postId IN :postIds")
    List<UUID> findRepostedPostIds(@Param("userId") UUID userId, @Param("postIds") Collection<UUID> postIds);
}
