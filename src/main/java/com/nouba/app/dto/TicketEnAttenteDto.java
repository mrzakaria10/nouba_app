package com.nouba.app.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketEnAttenteDto {
    private Long id;
    private String number;
    private String agencyName;
    private String clientName;
    private LocalDateTime dateCreation;
}
