package com.nouba.app.repositories;

import com.nouba.app.entities.Agency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgencyRepository extends JpaRepository<Agency, Long> {
    List<Agency> findByCityId(Long cityId);
}

