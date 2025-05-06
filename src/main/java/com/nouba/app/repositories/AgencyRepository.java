package com.nouba.app.repositories;

import com.nouba.app.entities.Agency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AgencyRepository extends JpaRepository<Agency, Long> {
    @Query("SELECT a FROM Agency a LEFT JOIN FETCH a.city LEFT JOIN FETCH a.user")
    List<Agency> findAll();

    @Query("SELECT a FROM Agency a LEFT JOIN FETCH a.city LEFT JOIN FETCH a.user WHERE a.city.id = :cityId")
    List<Agency> findByCityId(Long cityId);
}