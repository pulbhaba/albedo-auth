package com.akbo.auth.dao.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.akbo.auth.dao.entity.PasswordChangeRequest;

public interface PasswordResetRequestRepository extends JpaRepository<PasswordChangeRequest, Long> {

    @Query(" SELECT pcr\n" +
            "FROM   PasswordChangeRequest pcr\n" +
            "WHERE  (pcr.expired is null or pcr.expired = false)\n" +
            "AND    pcr.id = ?1\n" +
            "AND    pcr.randomString = ?2\n" +
            "AND    (pcr.passwordChanged is null or pcr.passwordChanged = false)")
    public Optional<PasswordChangeRequest> findOneByIdAndRandomStringNotExpired(final Long id,
            final String randomString);
}
