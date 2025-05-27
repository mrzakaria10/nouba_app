package com.nouba.app.dto;

import java.time.LocalDateTime;

public class TicketProcessingDto {
    private Long ticketId;  // Add this field
    private String ticketNumber;
    private String service;
    private LocalDateTime createdAt;
    private String clientName;
    private String status;

    // Updated constructor
    public TicketProcessingDto(Long ticketId, String ticketNumber, String service,
                               LocalDateTime createdAt, String clientName, String status) {
        this.ticketId = ticketId;
        this.ticketNumber = ticketNumber;
        this.service = service;
        this.createdAt = createdAt;
        this.clientName = clientName;
        this.status = status;
    }

    // Add getter for ticketId
    public Long getTicketId() {
        return ticketId;
    }

    // Add getters for all fields
    public String getTicketNumber() {
        return ticketNumber;
    }

    public String getService() {
        return service;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getClientName() {
        return clientName;
    }

    public String getStatus() {
        return status;
    }

}
