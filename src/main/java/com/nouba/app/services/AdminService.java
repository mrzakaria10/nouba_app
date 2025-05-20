package com.nouba.app.services;

import com.nouba.app.dto.AdminSummaryDTO;
import com.nouba.app.entities.Ticket;
import com.nouba.app.repositories.AgencyRepository;
import com.nouba.app.repositories.ClientRepository;
import com.nouba.app.repositories.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AgencyRepository agencyRepository;
    private final ClientRepository clientRepository;
    private final TicketRepository ticketRepository;

    /**
     * Retrieves summary statistics for admin dashboard
     * @return AdminSummaryDTO containing counts of agencies, clients, and pending tickets
     */
    public AdminSummaryDTO getAdminSummary() {
        long totalAgencies = agencyRepository.count();
        long totalClients = clientRepository.count();
        long totalPendingTickets = ticketRepository.countByStatus(Ticket.TicketStatus.EN_ATTENTE);

        return new AdminSummaryDTO(totalAgencies, totalClients, totalPendingTickets);
    }
}