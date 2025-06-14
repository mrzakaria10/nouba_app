package com.nouba.app.repositories;

import com.nouba.app.entities.Client;
import com.nouba.app.entities.Ticket;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    boolean existsByIdAndClientUserId(Long ticketId, Long userId);

    /**
     * Finds the maximum sequence number for an agency
     * يجد الحد الأقصى للرقم التسلسلي لوكالة
     */

    // Find max sequence for an agency
    @Query("SELECT MAX(t.sequenceNumber) FROM Ticket t WHERE t.agency.id = :agencyId")
    Optional<Integer> findMaxSequenceByAgency(@Param("agencyId") Long agencyId);

    @Query("SELECT COUNT(t) > 0 FROM Ticket t WHERE t.agency.id = :agencyId AND t.number = :number")
    boolean existsByAgencyIdAndNumber(@Param("agencyId") Long agencyId, @Param("number") String number);



    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Ticket t WHERE t.agency.id = :agencyId ORDER BY t.sequenceNumber DESC")
    List<Ticket> findLastTicketForAgency(@Param("agencyId") Long agencyId, Pageable pageable);

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
        @Query("SELECT t FROM Ticket t WHERE t.id = :ticketId AND t.agency.id = :agencyId")
        Optional<Ticket> findByIdAndAgencyId(@Param("ticketId") Long ticketId,
                                             @Param("agencyId") Long agencyId);

    @Query("SELECT t FROM Ticket t WHERE t.agency.id = :agencyId AND (t.status = 'ANNULE' OR t.status = 'TERMINE') ORDER BY t.issuedAt DESC")
    List<Ticket> findCompletedAndCancelledByAgencyId(@Param("agencyId") Long agencyId);

    /**
     * Find first ticket by agency ID and status, ordered by issuedAt descending
     * Trouver la première ticket par ID d'agence et statut, trié par date d'émission décroissante
     * العثور على أول تذكرة حسب معرف الوكالة والحالة، مرتبة حسب وقت الإصدار تنازلياً
     */
    @Query("SELECT t FROM Ticket t WHERE t.agency.id = :agencyId AND t.status = :status ORDER BY t.issuedAt DESC")
    Optional<Ticket> findFirstByAgencyIdAndStatusOrderByIssuedAtDesc(
            @Param("agencyId") Long agencyId,
            @Param("status") Ticket.TicketStatus status);

    // Add this method to TicketRepository.java
    @Query("SELECT COUNT(t) > 0 FROM Ticket t WHERE t.agency.id = :agencyId AND t.client.user.id = :userId")
    boolean existsByAgencyIdAndClientUserId(@Param("agencyId") Long agencyId, @Param("userId") Long userId);

    @Query("SELECT t FROM Ticket t WHERE t.agency.id = :agencyId AND t.status = 'EN_ATTENTE' AND t.client.user.id = :userId ORDER BY t.issuedAt DESC LIMIT 1")
    Optional<Ticket> findLastPendingTicketForClient(
            @Param("agencyId") Long agencyId,
            @Param("userId") Long userId);

    @Query("SELECT t FROM Ticket t WHERE t.agency.id = :agencyId AND t.status = 'EN_ATTENTE' ORDER BY t.issuedAt ASC LIMIT 1")
    Optional<Ticket> findFirstPendingTicketByAgency(@Param("agencyId") Long agencyId);

    @Modifying
    @Query("DELETE FROM Ticket t WHERE t.agency.id = :agencyId")
    void deleteAllByAgencyId(@Param("agencyId") Long agencyId);

    @Modifying
    @Query("DELETE FROM Ticket t WHERE t.client.id = :clientId")
    void deleteAllByClientId(@Param("clientId") Long clientId);

}

