package com.demo.userservice.Repositories;

import com.demo.userservice.Model.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<User,Long> {
    User findUserByUsername(String username);
    boolean existsByEmail(String email);

    User findByEmail(String email);
    boolean existsByUsername(String username);
}
