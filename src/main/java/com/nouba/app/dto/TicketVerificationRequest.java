package com.nouba.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TicketVerificationRequest {
    @NotBlank(message = "Ticket number is required")
    private String ticketNumber;

    private String city;
    private String agencyName;
    private String email; // Optional for notifications
}
