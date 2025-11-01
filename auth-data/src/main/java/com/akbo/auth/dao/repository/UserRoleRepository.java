package com.akbo.auth.dao.repository;

import com.akbo.auth.dao.entity.UserRole;
import com.akbo.auth.dto.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, Role> {

}
