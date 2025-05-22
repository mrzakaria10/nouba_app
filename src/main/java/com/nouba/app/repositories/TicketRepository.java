package com.nouba.app.repositories;

import com.nouba.app.entities.Client;
import com.nouba.app.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    boolean existsByIdAndClientUserId(Long ticketId, Long userId);

    /**
     * Finds the maximum sequence number for an agency
     * يجد الحد الأقصى للرقم التسلسلي لوكالة
     */
    @Query("SELECT MAX(CAST(SUBSTRING(t.number, 6) AS int)) FROM Ticket t WHERE t.agency.id = :agencyId")
    Optional<Integer> findMaxSequenceByAgency(@Param("agencyId") Long agencyId);

    /**
     * Counts pending tickets before a specific number
     * يحسب التذاكر المعلقة قبل رقم معين
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.agency.id = :agencyId AND CAST(SUBSTRING(t.number, 6) AS int) < :sequence AND t.status = 'EN_ATTENTE'")
    int countByAgencyIdAndSequenceLessThanAndPending(@Param("agencyId") Long agencyId,
                                                     @Param("sequence") Integer sequence);

    /**
     * Counts pending tickets for an agency
     * يحسب التذاكر المعلقة لوكالة
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.agency.id = :agencyId AND t.status = 'EN_ATTENTE'")
    int countPendingByAgencyId(@Param("agencyId") Long agencyId);

    /**
     * Finds the next ticket to serve
     * يجد التذكرة التالية للخدمة
     */
    @Query("SELECT t FROM Ticket t WHERE t.agency.id = :agencyId AND t.status = 'EN_ATTENTE' ORDER BY CAST(SUBSTRING(t.number, 6) AS int) ASC")
    Optional<Ticket> findNextPendingByAgencyId(@Param("agencyId") Long agencyId);

    /**
     * Finds a ticket by ID and user ID
     * يجد تذكرة بواسطة المعرف ومعرف المستخدم
     */
    @Query("SELECT t FROM Ticket t WHERE t.id = :ticketId AND t.client.user.id = :userId")
    Optional<Ticket> findByIdAndClientUserId(@Param("ticketId") Long ticketId,
                                             @Param("userId") Long userId);

    /**
     * Finds the currently serving ticket
     * يجد التذكرة قيد المعالجة حالياً
     */
    @Query("SELECT t FROM Ticket t WHERE t.agency.id = :agencyId AND t.status = 'EN_COURS'")
    Optional<Ticket> findCurrentlyServingByAgencyId(@Param("agencyId") Long agencyId);

    /**
     * Finds tickets by status
     * يجد التذاكر حسب الحالة
     */
    List<Ticket> findByStatus(Ticket.TicketStatus status);

    /**
     * Counts tickets by agency ID where served is false
     * يحسب التذاكر لوكالة حيث لم يتم الخدمة بعد
     */
    int countByAgencyIdAndServedFalse(Long agencyId);

    /**
     * Finds tickets by agency ID and served status
     * يجد التذاكر لوكالة وحالة الخدمة
     */
    List<Ticket> findByAgencyIdAndServed(Long agencyId, boolean served);

    /**
     * Counts tickets by agency ID and status
     * يحسب التذاكر لوكالة وحالتها
     */
    int countByAgencyIdAndStatus(Long agencyId, Ticket.TicketStatus status);

    // Add this method to your existing TicketRepository
    @Query("SELECT t FROM Ticket t WHERE t.issuedAt BETWEEN :start AND :end")
    List<Ticket> findByIssuedAtBetween(@Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    // Add to PUBLIC VERIFICATION
    Optional<Ticket> findByNumber(String number);

    // Add this method if not already present
    int countByStatus(Ticket.TicketStatus status);

    // Add this new method
    @Query("SELECT t FROM Ticket t WHERE t.number = :number AND t.agency.id = :agencyId AND t.agency.city.id = :cityId")
    Optional<Ticket> findByNumberAndAgencyAndCity(
            @Param("number") String number,
            @Param("agencyId") Long agencyId,
            @Param("cityId") Long cityId);

    // Add this to TicketRepository.java
    @Query("SELECT t FROM Ticket t " +
            "JOIN FETCH t.agency a " +
            "JOIN FETCH a.city " +
            "JOIN FETCH t.client c " +
            "JOIN FETCH c.user " +
            "WHERE DATE(t.issuedAt) = CURRENT_DATE " +
            "ORDER BY t.issuedAt DESC")
    List<Ticket> findAllTicketsReservedToday();
    /**
     * Finds all pending tickets for an agency
     * Trouver tous les tickets en attente pour une agence
     * العثور على جميع التذاكر المعلقة لوكالة
     */
    @Query("SELECT t FROM Ticket t WHERE t.agency.id = :agencyId AND t.status = 'EN_ATTENTE' ORDER BY CAST(SUBSTRING(t.number, 6) AS int) ASC")
    List<Ticket> findAllPendingByAgencyId(@Param("agencyId") Long agencyId);

    // Add to TicketRepository.java
    @Modifying
    @Query("DELETE FROM Ticket")
    void deleteAllTickets();

    // Add to TicketRepository.java
    @Query("SELECT t FROM Ticket t WHERE t.agency.id = :agencyId ORDER BY t.issuedAt DESC")
    List<Ticket> findAllByAgencyId(@Param("agencyId") Long agencyId);

    // Add to TicketRepository.java

    // Count EN_ATTENTE tickets for agency today
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.agency.id = :agencyId AND t.status = 'EN_ATTENTE' AND DATE(t.issuedAt) = CURRENT_DATE")
    int countEnAttenteTodayByAgency(@Param("agencyId") Long agencyId);

    // Count EN_COURS tickets for agency today
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.agency.id = :agencyId AND t.status = 'EN_COURS' AND DATE(t.issuedAt) = CURRENT_DATE")
    int countEnCoursTodayByAgency(@Param("agencyId") Long agencyId);

    // Count ANNULE tickets for agency today
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.agency.id = :agencyId AND t.status = 'ANNULE' AND DATE(t.issuedAt) = CURRENT_DATE")
    int countAnnuleTodayByAgency(@Param("agencyId") Long agencyId);

    // Count TERMINE tickets for agency today
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.agency.id = :agencyId AND t.status = 'TERMINE' AND DATE(t.issuedAt) = CURRENT_DATE")
    int countTermineTodayByAgency(@Param("agencyId") Long agencyId);



        // For endpoint 1
        List<Ticket> findByAgencyId(Long agencyId);

        // For endpoints 2, 4, 5
        Optional<Ticket> findByIdAndAgencyId(Long ticketId, Long agencyId);

        // For endpoint 6
        @Query("SELECT DISTINCT t.client FROM Ticket t WHERE t.agency.id = :agencyId")
        List<Client> findClientsByAgencyId(@Param("agencyId") Long agencyId);
    }

