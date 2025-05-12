package com.nouba.app.dto;

import com.nouba.app.entities.Ticket;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketDTO {
    private Long id;
    private String number;  // Format: NOUBA001 / التنسيق: NOUBA001
    private boolean served;
    private LocalDateTime issuedAt;  // Date de création / تاريخ الإنشاء
    private LocalDateTime servedAt;  // Date de service / تاريخ الخدمة
    private Long agencyId;
    private String agencyName;
    private Long clientId;
    private String clientName;

    /**
     * Convertit une entité Ticket en DTO
     * يحول كيان التذكرة إلى DTO
     */
    public static TicketDTO from(Ticket ticket) {
        TicketDTO dto = new TicketDTO();
        dto.setId(ticket.getId());
        dto.setNumber(ticket.getNumber());
        dto.setServed(ticket.isServed());
        dto.setIssuedAt(ticket.getIssuedAt());
        dto.setServedAt(ticket.getCompletedAt());  // Utilise completedAt comme servedAt
        dto.setAgencyId(ticket.getAgency().getId());
        dto.setAgencyName(ticket.getAgency().getName());
        dto.setClientId(ticket.getClient().getId());
        dto.setClientName(ticket.getClient().getUser().getName());
        return dto;
    }
}