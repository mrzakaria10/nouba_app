package com.nouba.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TicketCompleteDto {
    private String ticketNumber;
    private String service;
    private String clientName;
}