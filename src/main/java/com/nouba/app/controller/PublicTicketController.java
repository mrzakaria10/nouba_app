package com.nouba.app.controller;

import com.nouba.app.dto.ApiResponse;
import com.nouba.app.dto.TicketPublicDto;
import com.nouba.app.services.PublicTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/tickets")
@RequiredArgsConstructor
public class PublicTicketController {

    private final PublicTicketService publicTicketService;

    @PostMapping("/{cityId}/{agencyId}/verify")
    public ResponseEntity<ApiResponse<TicketPublicDto>> verifyTicket(
            @PathVariable Long cityId,
            @PathVariable Long agencyId,
            @RequestBody String ticketNumber) {

        TicketPublicDto response = publicTicketService.verifyTicket(
                ticketNumber,
                cityId,
                agencyId
        );

        return ResponseEntity.ok(
                new ApiResponse<>(response, "Ticket verified successfully", 200)
        );
    }
}