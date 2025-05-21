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

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.client " +
            "WHERE u.enabled = true " +
            "AND u.lastLogin BETWEEN :startOfWeek AND :endOfWeek " +
            "AND (u.role = 'CLIENT' OR u.role = 'AGENCY') " +
            "ORDER BY u.lastLogin DESC")
    List<User> findActiveUsersThisWeek(
            @Param("startOfWeek") LocalDateTime startOfWeek,
            @Param("endOfWeek") LocalDateTime endOfWeek);
}