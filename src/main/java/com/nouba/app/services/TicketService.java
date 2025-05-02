package com.nouba.app.services;

import com.nouba.app.entities.*;
import com.nouba.app.repositories.AgencyRepository;
import com.nouba.app.repositories.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketService {
    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    private final AgencyRepository agencyRepository;
    private final TicketRepository ticketRepository;
    private final EmailService emailService;

    /**
     * Génère un nouveau ticket pour une agence et un client
     */
    @Transactional
    public Ticket generateTicket(Long agencyId, Client client) {
        // Verify agency exists
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new RuntimeException("Agency not found"));

        // Get the last ticket number and increment
        Integer lastNumber = ticketRepository.findMaxNumberByAgencyAndUnserved(agencyId)
                .orElse(0);

        // Create and save new ticket
        Ticket ticket = new Ticket();
        ticket.setAgency(agency);
        ticket.setClient(client);
        ticket.setNumber(lastNumber + 1);
        ticket.setIssuedAt(LocalDateTime.now());
        ticket.setServed(false);

        Ticket savedTicket = ticketRepository.save(ticket);

        // Send notification
        sendTicketNotification(savedTicket);
        return savedTicket;
    }
    /**
     * Récupère le nombre de personnes devant un ticket donné
     */
    public int getPeopleAhead(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket non trouvé"));

        return ticketRepository.countByAgencyIdAndNumberLessThanAndServedFalse(
                ticket.getAgency().getId(), ticket.getNumber());
    }

    /**
     * Passe au client suivant dans la file d'attente
     */
    @Transactional
    public Ticket serveNextClient(Long agencyId) {
        Ticket nextTicket = ticketRepository.findTopByAgencyIdAndServedFalseOrderByNumberAsc(agencyId)
                .orElseThrow(() -> new RuntimeException("Aucun client dans la file d'attente"));

        nextTicket.setServed(true);
        nextTicket.setServedAt(LocalDateTime.now());

        return ticketRepository.save(nextTicket);
    }

    /**
     * Récupère le statut complet d'un ticket
     */
    public Map<String, Object> getTicketStatus(Long ticketId, Long userId) {
        Ticket ticket = ticketRepository.findByIdAndClientUserId(ticketId, userId)
                .orElseThrow(() -> new RuntimeException("Ticket non trouvé ou non autorisé"));

        int peopleAhead = getPeopleAhead(ticketId);
        int estimatedWaitMinutes = calculateWaitTime(ticket.getAgency().getId());

        Map<String, Object> status = new HashMap<>();
        status.put("ticketNumber", ticket.getNumber());
        status.put("agencyName", ticket.getAgency().getName());
        status.put("peopleAhead", peopleAhead);
        status.put("served", ticket.isServed());
        status.put("estimatedWaitMinutes", estimatedWaitMinutes);
        status.put("currentPosition", peopleAhead + 1);

        return status;
    }

    /**
     * Calcule le temps d'attente estimé
     */
    public int calculateWaitTime(Long agencyId) {
        int peopleInQueue = ticketRepository.countByAgencyIdAndServedFalse(agencyId);
        // Estimation: 5 minutes par personne
        return peopleInQueue * 5;
    }

    /**
     * Récupère le ticket actuellement en traitement
     */
    public Optional<Ticket> getCurrentTicket(Long agencyId) {
        return ticketRepository.findTopByAgencyIdAndServedFalseOrderByNumberAsc(agencyId);
    }

    /**
     * Crée un ticket avec statut "en attente" et envoie une notification
     */
    @Transactional
    public Ticket createTicketWithStatusPending(Long agencyId, Client client) {
        Ticket ticket = generateTicket(agencyId, client);
        sendTicketNotification(ticket);
        return ticket;
    }

    /**
     * Envoie une notification de création de ticket
     */
    private void sendTicketNotification(Ticket ticket) {
        try {
            Map<String, String> values = new HashMap<>();
            values.put("clientName", ticket.getClient().getUser().getName());
            values.put("ticketNumber", String.valueOf(ticket.getNumber()));
            values.put("agencyName", ticket.getAgency().getName());
            values.put("peopleAhead", String.valueOf(getPeopleAhead(ticket.getId())));
            values.put("estimatedWait", String.valueOf(calculateWaitTime(ticket.getAgency().getId())));

            String content = emailService.loadEmailTemplate("templates.email/ticket-notification.html", values);
            emailService.sendEmail(
                    ticket.getClient().getUser().getEmail(),
                    "Votre ticket pour " + ticket.getAgency().getName(),
                    content
            );
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification", e);
        }
    }

    /**
     * Vérifie périodiquement les tickets en attente (toutes les 3 minutes)
     */
    @Scheduled(fixedRate = 180000)
    public void checkPendingTickets() {
        List<Ticket> pendingTickets = ticketRepository.findByServedFalse();

        for (Ticket ticket : pendingTickets) {
            int peopleAhead = getPeopleAhead(ticket.getId());
            if (peopleAhead <= 5) {
                sendApproachingNotification(ticket, peopleAhead);
            }
        }
    }

    /**
     * Envoie une notification quand le tour approche
     */
    private void sendApproachingNotification(Ticket ticket, int peopleAhead) {
        try {
            Map<String, String> values = new HashMap<>();
            values.put("clientName", ticket.getClient().getUser().getName());
            values.put("ticketNumber", String.valueOf(ticket.getNumber()));
            values.put("agencyName", ticket.getAgency().getName());
            values.put("peopleAhead", String.valueOf(peopleAhead));
            values.put("estimatedWait", String.valueOf(peopleAhead * 5));

            String content = emailService.loadEmailTemplate("templates.email/ticket-approaching.html", values);
            emailService.sendEmail(
                    ticket.getClient().getUser().getEmail(),
                    "Votre tour approche (" + ticket.getAgency().getName() + ")",
                    content
            );
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification d'approche", e);
        }
    }
}