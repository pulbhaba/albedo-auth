package com.akbo.auth.dao.repository;

import com.akbo.auth.dao.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // @Query("select u, r from User u join Role r on r.user_id=u.id where
    // u.username=?1")
    Optional<User> findByUsername(final String username);
}
