package com.nouba.app.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketReserveDto {
    private Long id;
    private String number;
    private String agencyName;
    private String clientName;
    private String status;
    private LocalDateTime dateCreation;
}
