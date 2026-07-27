package com.akbo.auth.dao.entity;

import com.akbo.auth.dto.Role;
import com.akbo.auth.dto.RoleUpdateRequestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Audited(withModifiedFlag = true)
@Table(
        name = "role_update_requests",
        indexes = {
            @Index(name = "idx_role_update_requests_user_status", columnList = "user_id,status"),
            @Index(name = "idx_role_update_requests_status", columnList = "status")
        })
@EqualsAndHashCode(callSuper = true)
public class RoleUpdateRequest extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private Role requestedRole;

    @Column(length = 2048)
    private String evidence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RoleUpdateRequestStatus status;

    @Column(length = 255)
    private String resolvedBy;

    @Column(columnDefinition = "datetime")
    private LocalDateTime resolvedAt;

    @Column(length = 1024)
    private String resolutionReason;
}
