// PublicTicketController.java
package com.nouba.app.controller;

import com.nouba.app.dto.ApiResponse;
import com.nouba.app.dto.TicketPublicDto;
import com.nouba.app.dto.TicketVerificationRequest;
import com.nouba.app.services.EmailService;
import com.nouba.app.services.PublicTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/tickets")
@RequiredArgsConstructor
public class PublicTicketController {

    private final PublicTicketService publicTicketService;

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<TicketPublicDto>> verifyTicket(
            @RequestBody TicketVerificationRequest request) {

        TicketPublicDto response = publicTicketService.verifyTicket(
                request.getTicketNumber(),
                request.getCity(),
                request.getAgencyName()

        );

       /** // Send notification if email provided
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            emailService.sendTicketVerificationEmail(
                    request.getEmail(),
                    response.getTicketNumber(),
                    response.getClientName(),
                    response.getAgencyName(),
                    response.getCity(),
                    response.getStatus(),
                    response.getPositionInQueue(),
                    response.getEstimatedWaitTime()
            );
        }*/

        return ResponseEntity.ok(
                new ApiResponse<>(response, "Ticket verified successfully", 200)
        );
    }
}