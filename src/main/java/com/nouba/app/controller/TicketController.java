package com.nouba.app.controller;

import com.nouba.app.dto.ApiResponse;
import com.nouba.app.entities.*;
import com.nouba.app.repositories.ClientRepository;
import com.nouba.app.services.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final ClientRepository clientRepository;

    @PostMapping("/agency/{agencyId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> takeTicket(
            @PathVariable Long agencyId,
            @AuthenticationPrincipal User user) {

        Client client = clientRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

        Ticket ticket = ticketService.generateTicket(agencyId, client);
        Map<String, Object> status = ticketService.getTicketStatus(ticket.getId(), user.getId());

        return ResponseEntity.ok(
                new ApiResponse<>(status, "Ticket créé avec succès", 200));
    }

    @GetMapping("/{ticketId}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTicketStatus(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal User user) {

        Map<String, Object> status = ticketService.getTicketStatus(ticketId, user.getId());
        return ResponseEntity.ok(
                new ApiResponse<>(status, "Statut du ticket récupéré", 200));
    }

    @GetMapping("/{ticketId}/ahead")
    public ResponseEntity<ApiResponse<Integer>> getPeopleAhead(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal User user) {

        ticketService.getTicketStatus(ticketId, user.getId()); // Vérifie l'accès
        int peopleAhead = ticketService.getPeopleAhead(ticketId);

        return ResponseEntity.ok(
                new ApiResponse<>(peopleAhead, "Nombre de personnes devant vous", 200));
    }

    @PutMapping("/agency/{agencyId}/serve")
    public ResponseEntity<ApiResponse<Map<String, Object>>> serveNextClient(
            @PathVariable Long agencyId) {

        Ticket servedTicket = ticketService.serveNextClient(agencyId);
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("ticketNumber", servedTicket.getNumber());
        response.put("clientName", servedTicket.getClient().getUser().getName());
        response.put("servedAt", servedTicket.getServedAt());

        return ResponseEntity.ok(
                new ApiResponse<>(response, "Client suivant servi avec succès", 200));
    }

    @GetMapping("/agency/{agencyId}/current")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentTicket(
            @PathVariable Long agencyId) {

        return ticketService.getCurrentTicket(agencyId)
                .map(ticket -> {
                    Map<String, Object> response = new java.util.HashMap<>();
                    response.put("ticketNumber", ticket.getNumber());
                    response.put("clientName", ticket.getClient().getUser().getName());
                    return ResponseEntity.ok(
                            new ApiResponse<>(response, "Ticket en cours de traitement", 200));
                })
                .orElseGet(() -> ResponseEntity.ok(
                        new ApiResponse<>("Aucun ticket en attente", 200)));
    }
}