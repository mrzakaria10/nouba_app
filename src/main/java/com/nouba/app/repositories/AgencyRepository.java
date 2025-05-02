package com.nouba.app.repositories;

import com.nouba.app.entities.Agency;
import com.nouba.app.entities.City;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AgencyRepository extends JpaRepository<Agency, Long> {
    List<Agency> findByCityId(Long cityId);

    // Requête personnalisée pour charger les relations
    @Query("SELECT DISTINCT a FROM Agency a " +
            "LEFT JOIN FETCH a.city " +
            "LEFT JOIN FETCH a.user " +
            "LEFT JOIN FETCH a.tickets")  // Si vous avez besoin des tickets)
    List<Agency> findAllWithRelations();
}

