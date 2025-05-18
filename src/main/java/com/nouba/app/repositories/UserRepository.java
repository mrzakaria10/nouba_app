package com.nouba.app.repositories;

import com.nouba.app.entities.Role;
import com.nouba.app.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByActivationToken(String token);

    // Add this new method for admin dashboard
    List<User> findByLastLoginBetween(LocalDateTime start, LocalDateTime end);
    List<User> findByRole(Role role); // Add this new method

}