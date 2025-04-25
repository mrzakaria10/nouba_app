package com.nouba.app.controller;

import com.nouba.app.dto.ApiResponse;
import com.nouba.app.dto.TicketResponseDTO;
import com.nouba.app.entities.*;
import com.nouba.app.repositories.ClientRepository;
import com.nouba.app.services.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/tickets") // Changed from /auth/login/tickets
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final ClientRepository clientRepository;

    @PostMapping("/create/agency/{agencyId}")
    public ResponseEntity<ApiResponse<TicketResponseDTO>> takeTicket(
            @PathVariable Long agencyId,
            @AuthenticationPrincipal User user) {

        // Find client associated with the authenticated user
        Client client = clientRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No client profile found. Please create a client profile first."));

        // Generate the ticket
        Ticket ticket = ticketService.generateTicket(agencyId, client);
        int peopleAhead = ticketService.getPeopleAhead(ticket.getId());
        int estimatedWait = ticketService.calculateWaitTime(ticket.getAgency().getId());

        // Create response DTO
        TicketResponseDTO response = new TicketResponseDTO(ticket, peopleAhead, estimatedWait);

        return ResponseEntity.ok(
                new ApiResponse<>(response, "Ticket created successfully", 200));
    }

    @GetMapping("/status/{ticketId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTicketStatus(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal User user) {

        Map<String, Object> status = ticketService.getTicketStatus(ticketId, user.getId());
        return ResponseEntity.ok(
                new ApiResponse<>(status, "Ticket status retrieved", 200));
    }

    @GetMapping("/{ticketId}/ahead")
    public ResponseEntity<ApiResponse<Integer>> getPeopleAhead(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal User user) {

        ticketService.getTicketStatus(ticketId, user.getId()); // Verify access
        int peopleAhead = ticketService.getPeopleAhead(ticketId);

        return ResponseEntity.ok(
                new ApiResponse<>(peopleAhead, "Number of people ahead", 200));
    }

    @PutMapping("/serve/agency/{agencyId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> serveNextClient(
            @PathVariable Long agencyId) {

        Ticket servedTicket = ticketService.serveNextClient(agencyId);
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("ticketNumber", servedTicket.getNumber());
        response.put("clientName", servedTicket.getClient().getUser().getName());
        response.put("servedAt", servedTicket.getServedAt());

        return ResponseEntity.ok(
                new ApiResponse<>(response, "Next client served successfully", 200));
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
                            new ApiResponse<>(response, "Currently serving ticket", 200));
                })
                .orElseGet(() -> ResponseEntity.ok(
                        new ApiResponse<>("No tickets in queue", 200)));
    }
}