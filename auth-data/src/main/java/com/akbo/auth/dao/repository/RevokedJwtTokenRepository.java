package com.akbo.auth.dao.repository;

import com.akbo.auth.dao.entity.RevokedJwtToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface RevokedJwtTokenRepository extends JpaRepository<RevokedJwtToken, String> {

    boolean existsByTokenIdAndExpiresAtAfter(String tokenId, Instant expiresAt);

    long deleteByExpiresAtBefore(Instant expiresAt);
}
