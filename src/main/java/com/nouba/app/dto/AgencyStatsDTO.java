package com.nouba.app.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgencyStatsDTO {
    private Long agencyId;
    private String agencyName;
    private int totalClients;
    private int pendingTickets;
    private int completedTickets;
    private int inProgressTickets;
}