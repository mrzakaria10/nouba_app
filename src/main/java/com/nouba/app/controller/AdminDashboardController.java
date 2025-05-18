package com.nouba.app.controller;

import com.nouba.app.dto.*;
import com.nouba.app.services.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor

public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/agences-actives")
    public ResponseEntity<ApiResponse<List<AgencyActiveDto>>> getAllAgencesActives() {
        List<AgencyActiveDto> agences = adminDashboardService.getAllAgencesActives();
        return ResponseEntity.ok(
                new ApiResponse<>(agences, "Liste des agences actives récupérée avec succès", 200));
    }

    @GetMapping("/clients-connectes")
    public ResponseEntity<ApiResponse<List<ClientConnectedDto>>> getAllClientsConnectesAujourdhui() {
        List<ClientConnectedDto> clients = adminDashboardService.getAllClientsConnectesAujourdhui();
        return ResponseEntity.ok(
                new ApiResponse<>(clients, "Liste des clients connectés aujourd'hui", 200));
    }

    @GetMapping("/tickets-en-attente")
    public ResponseEntity<ApiResponse<List<TicketEnAttenteDto>>> getAllTicketsEnAttente() {
        List<TicketEnAttenteDto> tickets = adminDashboardService.getAllTicketsEnAttente();
        return ResponseEntity.ok(
                new ApiResponse<>(tickets, "Liste des tickets en attente", 200));
    }

    @GetMapping("/tickets-reserves")
    public ResponseEntity<ApiResponse<List<TicketReserveDto>>> getAllTicketsReservesAujourdhui() {
        List<TicketReserveDto> tickets = adminDashboardService.getAllTicketsReservesAujourdhui();
        return ResponseEntity.ok(
                new ApiResponse<>(tickets, "Liste des tickets réservés aujourd'hui", 200));
    }
}