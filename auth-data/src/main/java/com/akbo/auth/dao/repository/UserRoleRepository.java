package com.akbo.auth.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.akbo.auth.dao.entity.UserRole;
import com.akbo.auth.dto.Role;

public interface UserRoleRepository extends JpaRepository<UserRole, Role> {

}
