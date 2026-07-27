package com.akbo.auth.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "revoked_jwt_tokens")
public class RevokedJwtToken {

    @Id
    @Column(nullable = false, length = 128)
    private String tokenId;

    @Column(nullable = false)
    private Instant expiresAt;
}
