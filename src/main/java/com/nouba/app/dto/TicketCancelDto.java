package com.nouba.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class TicketCancelDto {
    private String clientName;
    private String ticketNumber;
}