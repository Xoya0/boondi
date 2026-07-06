package com.boondi.domain.repository;

import com.boondi.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    // Matches on username or display name, case-insensitive substring
    @Query(value = "SELECT * FROM users u WHERE u.deleted_at IS NULL AND " +
                   "(u.username ILIKE CONCAT('%', :query, '%') OR u.display_name ILIKE CONCAT('%', :query, '%')) " +
                   "ORDER BY u.username ASC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<User> searchUsers(@Param("query") String query,
                           @Param("limit") int limit,
                           @Param("offset") int offset);
}
