package com.akbo.auth.dao.repository;

import com.akbo.auth.dao.entity.RoleUpdateRequest;
import com.akbo.auth.dto.Role;
import com.akbo.auth.dto.RoleUpdateRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleUpdateRequestRepository extends JpaRepository<RoleUpdateRequest, Long> {

    boolean existsByUserUsernameAndRequestedRoleAndStatus(
            String username, Role requestedRole, RoleUpdateRequestStatus status);

    List<RoleUpdateRequest> findByUserUsernameOrderByCreatedTimeDesc(String username);

    List<RoleUpdateRequest> findByStatusOrderByCreatedTimeDesc(RoleUpdateRequestStatus status);

    List<RoleUpdateRequest> findAllByOrderByCreatedTimeDesc();
}
