package com.nouba.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

// TicketAgencyDto.java - For endpoint 1
@Data
@AllArgsConstructor
public class TicketAgencyDto {
    private String ticketNumber;
    private String service;
    private LocalDateTime createdAt;
    private int position;
    private String estimatedTime;
    private String status;
}

