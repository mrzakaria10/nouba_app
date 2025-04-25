package com.nouba.app.dto;

import com.nouba.app.entities.Ticket;
import lombok.Data;

@Data
public class TicketResponseDTO {
    private Long ticketId;
    private Integer ticketNumber;
    private String agencyName;
    private Integer peopleAhead;
    private Integer estimatedWaitMinutes;
    private Integer currentPosition;
    private Boolean served;

    // You can add a constructor to easily create from Ticket entity
    public TicketResponseDTO(Ticket ticket, int peopleAhead, int estimatedWaitMinutes) {
        this.ticketId = ticket.getId();
        this.ticketNumber = ticket.getNumber();
        this.agencyName = ticket.getAgency().getName();
        this.peopleAhead = peopleAhead;
        this.estimatedWaitMinutes = estimatedWaitMinutes;
        this.currentPosition = peopleAhead + 1;
        this.served = ticket.isServed();
    }
}