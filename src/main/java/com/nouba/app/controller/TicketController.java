package com.nouba.app.controller;

import com.nouba.app.entities.Client;
import com.nouba.app.entities.Ticket;
import com.nouba.app.entities.User;
import com.nouba.app.repositories.ClientRepository;
import com.nouba.app.services.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final ClientRepository clientRepository;

    @PostMapping("/agency/{agencyId}")
    public Ticket takeTicket(@PathVariable Long agencyId,
                             @AuthenticationPrincipal User user) {
        Client client = clientRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        return ticketService.generateTicket(agencyId, client);
    }

    @GetMapping("/{ticketId}/ahead")
    public int getPeopleAhead(@PathVariable Long ticketId) {
        return ticketService.getPeopleAhead(ticketId);
    }

    @PutMapping("/agency/{agencyId}/serve")
    public Ticket serveNextClient(@PathVariable Long agencyId) {
        return ticketService.serveNextClient(agencyId);
    }



}