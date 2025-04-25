package com.nouba.app.services;

import com.nouba.app.entities.*;
import com.nouba.app.repositories.AgencyRepository;
import com.nouba.app.repositories.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final AgencyRepository agencyRepository;
    private final TicketRepository ticketRepository;


    @Transactional
    public Ticket generateTicket(Long agencyId, Client client) {
        // Verify agency exists
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new RuntimeException("Agence non trouvée avec l'ID: " + agencyId));

        // Get last ticket number and increment by 1
        Integer lastNumber = ticketRepository.findMaxNumberByAgencyAndUnserved(agencyId)
                .orElse(0);

        try {
            // Create and save new ticket
            Ticket ticket = new Ticket();
            ticket.setAgency(agency);
            ticket.setClient(client);
            ticket.setNumber(lastNumber + 1);
            ticket.setIssuedAt(LocalDateTime.now());
            ticket.setServed(false);

            return ticketRepository.save(ticket);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du ticket: " + e.getMessage());
        }
    }


    public int getPeopleAhead(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket non trouvé"));

        return ticketRepository.countByAgencyIdAndNumberLessThanAndServedFalse(
                ticket.getAgency().getId(), ticket.getNumber());
    }

    @Transactional
    public Ticket serveNextClient(Long agencyId) {
        Ticket nextTicket = ticketRepository.findTopByAgencyIdAndServedFalseOrderByNumberAsc(agencyId)
                .orElseThrow(() -> new RuntimeException("Aucun client dans la file d'attente"));

        nextTicket.setServed(true);
        nextTicket.setServedAt(LocalDateTime.now());

        return ticketRepository.save(nextTicket);
    }

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
        status.put("currentPosition", peopleAhead + 1); // Position dans la file

        return status;
    }

    public int calculateWaitTime(Long agencyId) {
        int peopleInQueue = ticketRepository.countByAgencyIdAndServedFalse(agencyId);
        // Estimation: 5 minutes par personne
        return peopleInQueue * 5;
    }

    public Optional<Ticket> getCurrentTicket(Long agencyId) {
        return ticketRepository.findTopByAgencyIdAndServedFalseOrderByNumberAsc(agencyId);
    }
}