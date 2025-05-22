package com.nouba.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TicketServiceDto {
    private String ticketNumber;
    private String service;
    private LocalDateTime createdAt;
    private String clientName;
}