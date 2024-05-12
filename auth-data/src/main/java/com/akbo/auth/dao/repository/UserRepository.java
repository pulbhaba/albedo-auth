package com.akbo.auth.dao.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;

import com.akbo.auth.dao.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {

    // @Query("select u, r from User u join Role r on r.user_id=u.id where
    // u.username=?1")
    public User findByUsername(String username);

}
