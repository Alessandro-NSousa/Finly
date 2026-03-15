package com.finly.repository;

import com.finly.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.user.id = :userId AND t.used = false")
    void deleteActiveTokensByUserId(Long userId);

    void deleteByExpiryDateBefore(LocalDateTime date);
}
