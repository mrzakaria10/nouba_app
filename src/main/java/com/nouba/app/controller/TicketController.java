package com.nouba.app.controller;

import com.nouba.app.dto.ApiResponse;
import com.nouba.app.dto.TicketDTO;
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

    /**
     * Endpoint to create a new ticket for an agency
     * @param agencyId ID of the agency
     * @return Response with created ticket details
     */
    @PostMapping("/agency/{agencyId}/{clientId}")
    public ResponseEntity<ApiResponse<TicketDTO>> takeTicket(
            @PathVariable Long agencyId,
            @PathVariable Long clientId) {
        // Find client associated with the user
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        // Generate the ticket
        Ticket ticket = ticketService.generateTicket(agencyId, client);

        // Convert to DTO and return response
        return ResponseEntity.ok(
                new ApiResponse<>(TicketDTO.from(ticket), "Ticket created successfully", 200));
    }

    /**
     * Get the status of a specific ticket
     */
    @GetMapping("/{ticketId}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTicketStatus(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal User user) {

        Map<String, Object> status = ticketService.getTicketStatus(ticketId, user.getId());
        return ResponseEntity.ok(
                new ApiResponse<>(status, "Ticket status retrieved", 200));
    }

    /**
     * Get number of people ahead in queue
     */
    @GetMapping("/{ticketId}/ahead")
    public ResponseEntity<ApiResponse<Integer>> getPeopleAhead(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal User user) {

        ticketService.getTicketStatus(ticketId, user.getId()); // Verify access
        int peopleAhead = ticketService.getPeopleAhead(ticketId);

        return ResponseEntity.ok(
                new ApiResponse<>(peopleAhead, "Number of people ahead", 200));
    }

    /**
     * Serve the next client in queue
     */
    @PutMapping("/agency/{agencyId}/serve")
    public ResponseEntity<ApiResponse<Map<String, Object>>> serveNextClient(
            @PathVariable Long agencyId) {

        Ticket servedTicket = ticketService.serveNextClient(agencyId);
        Map<String, Object> response = Map.of(
                "ticketNumber", servedTicket.getNumber(),
                "clientName", servedTicket.getClient().getUser().getName(),
                "servedAt", servedTicket.getServedAt()
        );

        return ResponseEntity.ok(
                new ApiResponse<>(response, "Next client served", 200));
    }

    /**
     * Get current ticket being served
     */
    @GetMapping("/agency/{agencyId}/current")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentTicket(
            @PathVariable Long agencyId) {

        return ticketService.getCurrentTicket(agencyId)
                .map(ticket -> {
                    Map<String, Object> response = Map.of(
                            "ticketNumber", ticket.getNumber(),
                            "clientName", ticket.getClient().getUser().getName()
                    );
                    return ResponseEntity.ok(
                            new ApiResponse<>(response, "Current ticket", 200));
                })
                .orElseGet(() -> ResponseEntity.ok(
                        new ApiResponse<>("No tickets in queue", 200)));
    }
}