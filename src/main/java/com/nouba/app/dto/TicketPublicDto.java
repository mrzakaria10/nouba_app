// TicketPublicDto.java
package com.nouba.app.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TicketPublicDto {
    private String ticketNumber;
    private String clientName;
    private String agencyName;
    private String city;
    private String status;
    private LocalDateTime issuedAt;
    private int positionInQueue;
    private String estimatedWaitTime;


}