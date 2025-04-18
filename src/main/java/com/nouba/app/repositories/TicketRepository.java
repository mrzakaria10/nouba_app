package com.nouba.app.repositories;

import com.nouba.app.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT MAX(t.number) FROM Ticket t WHERE t.agency.id = :agencyId AND t.served = false")
    Optional<Integer> findMaxNumberByAgencyAndUnserved(@Param("agencyId") Long agencyId);

    int countByAgencyIdAndNumberLessThanAndServedFalse(Long agencyId, Integer number);

    Optional<Ticket> findTopByAgencyIdAndServedFalseOrderByNumberAsc(Long agencyId);

}