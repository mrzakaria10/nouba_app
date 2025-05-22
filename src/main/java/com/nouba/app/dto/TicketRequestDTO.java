package com.nouba.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketRequestDTO {
    @NotNull(message = "Agency ID is required")
    private Long agencyId;

    @NotNull(message = "AgencyService ID is required")
    private Long serviceId;
}