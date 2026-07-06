package com.boondi.domain.repository;

import com.boondi.domain.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, UUID> {

    Optional<EmailVerification> findByTokenHash(String tokenHash);

    @Modifying
    @Query("DELETE FROM EmailVerification e WHERE e.user.id = :userId AND e.isUsed = false")
    void deleteByUserIdAndIsUsedFalse(UUID userId);
}
