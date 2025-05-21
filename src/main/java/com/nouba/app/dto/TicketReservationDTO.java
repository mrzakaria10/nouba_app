package com.nouba.app.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TicketReservationDTO {
    private String ticketNumber;
    private String clientName;
    private String clientEmail;
    private String agencyName;
    private String city;
    private LocalDateTime issuedAt;
    private String timeAgo; // e.g. "5 minutes ago"
}