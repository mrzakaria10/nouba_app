package com.nouba.app.dto;

import com.nouba.app.entities.Ticket;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketDTO {
    private Long id;
    private Integer number;
    private boolean served;
    private LocalDateTime issuedAt;
    private LocalDateTime servedAt;
    private Long agencyId;
    private String agencyName;
    private Long clientId;
    private String clientName;

    public static TicketDTO from(Ticket ticket) {
        TicketDTO dto = new TicketDTO();
        dto.setId(ticket.getId());
        dto.setNumber(ticket.getNumber());
        dto.setServed(ticket.isServed());
        dto.setIssuedAt(ticket.getIssuedAt());
        dto.setServedAt(ticket.getServedAt());
        dto.setAgencyId(ticket.getAgency().getId());
        dto.setAgencyName(ticket.getAgency().getName());
        dto.setClientId(ticket.getClient().getId());
        dto.setClientName(ticket.getClient().getUser().getName());
        return dto;
    }
}