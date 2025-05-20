package com.nouba.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminSummaryDTO {
    private long totalAgencies;
    private long totalClients;
    private long totalPendingTickets;
}