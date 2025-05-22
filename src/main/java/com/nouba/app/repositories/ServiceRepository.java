package com.nouba.app.repositories;

import com.nouba.app.entities.AgencyService;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ServiceRepository extends JpaRepository<AgencyService, Long> {
    List<AgencyService> findByAgenciesId(Long agencyId);
    Optional<AgencyService> findByIdAndAgenciesId(Long id, Long agencyId);
}