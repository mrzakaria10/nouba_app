package com.nouba.app.repositories;



import com.nouba.app.entities.Client;
import com.nouba.app.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByUser(User user);
    Optional<Client> findById(Long id);


    @Query("SELECT COUNT(DISTINCT c) FROM Client c JOIN c.tickets t WHERE t.agency.id = :agencyId")
    int countDistinctByTicketsAgencyId(@Param("agencyId") Long agencyId);
}