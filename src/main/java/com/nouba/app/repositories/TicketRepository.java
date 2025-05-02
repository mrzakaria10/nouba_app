package com.nouba.app.repositories;

import com.nouba.app.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT MAX(t.number) FROM Ticket t WHERE t.agency.id = :agencyId AND t.served = false")
    Optional<Integer> findMaxNumberByAgencyAndUnserved(@Param("agencyId") Long agencyId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.agency.id = :agencyId AND t.number < :number AND t.served = false")
    int countByAgencyIdAndNumberLessThanAndServedFalse(@Param("agencyId") Long agencyId,
                                                       @Param("number") Integer number);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.agency.id = :agencyId AND t.served = false")
    int countByAgencyIdAndServedFalse(@Param("agencyId") Long agencyId);

    @Query("SELECT t FROM Ticket t WHERE t.agency.id = :agencyId AND t.served = false ORDER BY t.number ASC")
    Optional<Ticket> findTopByAgencyIdAndServedFalseOrderByNumberAsc(@Param("agencyId") Long agencyId);

    @Query("SELECT t FROM Ticket t WHERE t.id = :ticketId AND t.client.user.id = :userId")
    Optional<Ticket> findByIdAndClientUserId(@Param("ticketId") Long ticketId,
                                             @Param("userId") Long userId);
    // Ajout de la méthode manquante
    List<Ticket> findByServedFalse();
}