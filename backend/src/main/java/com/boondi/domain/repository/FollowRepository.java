package com.boondi.domain.repository;

import com.boondi.domain.entity.Follow;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, Follow.FollowId> {

    boolean existsByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

    void deleteByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

    // All follower ids of a user — used to invalidate their home timeline caches
    @Query("SELECT f.followerId FROM Follow f WHERE f.followeeId = :followeeId")
    List<UUID> findFollowerIds(@Param("followeeId") UUID followeeId);

    // Followers of :userId, newest follow first. Rows: [User, follow.createdAt (cursor)]
    @Query("SELECT u, f.createdAt FROM Follow f JOIN User u ON u.id = f.followerId " +
           "WHERE f.followeeId = :userId " +
           "AND (cast(:cursor as timestamp) IS NULL OR f.createdAt < :cursor) " +
           "ORDER BY f.createdAt DESC")
    List<Object[]> findFollowers(@Param("userId") UUID userId,
                                 @Param("cursor") OffsetDateTime cursor,
                                 Pageable pageable);

    // Users that :userId follows, newest follow first. Rows: [User, follow.createdAt (cursor)]
    @Query("SELECT u, f.createdAt FROM Follow f JOIN User u ON u.id = f.followeeId " +
           "WHERE f.followerId = :userId " +
           "AND (cast(:cursor as timestamp) IS NULL OR f.createdAt < :cursor) " +
           "ORDER BY f.createdAt DESC")
    List<Object[]> findFollowing(@Param("userId") UUID userId,
                                 @Param("cursor") OffsetDateTime cursor,
                                 Pageable pageable);
}
