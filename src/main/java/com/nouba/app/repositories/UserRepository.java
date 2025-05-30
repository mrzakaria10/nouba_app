package com.nouba.app.repositories;

import com.nouba.app.entities.Role;
import com.nouba.app.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByActivationToken(String token);

    List<User> findByLastLoginBetween(LocalDateTime start, LocalDateTime end);
    List<User> findByRole(Role role);

    @Query("SELECT u FROM User u WHERE u.enabled = true AND u.activatedAt BETWEEN :startDate AND :endDate")
    List<User> findActiveUsersThisWeek(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}