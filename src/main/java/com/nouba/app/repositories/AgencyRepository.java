package com.nouba.app.repositories;

import com.nouba.app.entities.Agency;
import com.nouba.app.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AgencyRepository extends JpaRepository<Agency, Long> {
    Optional<Agency> findByNameIgnoreCase(String name);

    // Add this method to find agency by user
    @Query("SELECT a FROM Agency a WHERE a.user = :user")
    Optional<Agency> findByUser(@Param("user") User user);

    // Keep your existing methods
    @Query("SELECT a FROM Agency a LEFT JOIN FETCH a.city LEFT JOIN FETCH a.user")
    List<Agency> findAll();

    @Query("SELECT a FROM Agency a LEFT JOIN FETCH a.city LEFT JOIN FETCH a.user WHERE a.city.id = :cityId")
    List<Agency> findByCityId(Long cityId);

    @Query("SELECT DISTINCT a FROM Agency a JOIN a.tickets t WHERE t.issuedAt BETWEEN :debut AND :fin")
    List<Agency> findActiveAgencies(@Param("debut") LocalDateTime debut,
                                    @Param("fin") LocalDateTime fin);

    Optional<Agency> findByUserId(Long userId);

    Optional<Agency> findByUser_Id(Long userId);
}