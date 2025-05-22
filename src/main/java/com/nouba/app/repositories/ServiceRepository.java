package com.nouba.app.repositories;

import com.nouba.app.entities.Servicee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Servicee, Long> {
    List<Servicee> findByAgenciesId(Long agencyId);
    Optional<Servicee> findByIdAndAgenciesId(Long id, Long agencyId);
}