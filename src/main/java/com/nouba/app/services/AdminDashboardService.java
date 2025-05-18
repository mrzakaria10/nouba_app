package com.nouba.app.services;

import com.nouba.app.dto.AgencyActiveDto;
import com.nouba.app.dto.ClientConnectedDto;
import com.nouba.app.dto.TicketEnAttenteDto;
import com.nouba.app.dto.TicketReserveDto;
import com.nouba.app.entities.*;
import com.nouba.app.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final AgencyRepository agencyRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    public List<AgencyActiveDto> getAllAgencesActives() {
        LocalDate aujourdhui = LocalDate.now();
        return agencyRepository.findActiveAgencies(aujourdhui.atStartOfDay(), aujourdhui.plusDays(1).atStartOfDay())
                .stream()
                .map(agence -> {
                    AgencyActiveDto dto = new AgencyActiveDto();
                    dto.setId(agence.getId());
                    dto.setName(agence.getName());
                    dto.setCity(agence.getCity().getName()); // Assuming City has getName()
                    dto.setTicketsToday(agence.getTickets().size());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<ClientConnectedDto> getAllClientsConnectesAujourdhui() {
        LocalDate aujourdhui = LocalDate.now();
        return userRepository.findByLastLoginBetween(aujourdhui.atStartOfDay(), aujourdhui.plusDays(1).atStartOfDay())
                .stream()
                .filter(user -> user.getRole() == Role.CLIENT)
                .map(user -> {
                    ClientConnectedDto dto = new ClientConnectedDto();
                    dto.setId(user.getId());
                    dto.setName(user.getName());
                    dto.setEmail(user.getEmail());
                    dto.setLastLogin(user.getLastLogin());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<TicketEnAttenteDto> getAllTicketsEnAttente() {
        return ticketRepository.findByStatus(Ticket.TicketStatus.EN_ATTENTE)
                .stream()
                .map(ticket -> {
                    TicketEnAttenteDto dto = new TicketEnAttenteDto();
                    dto.setId(ticket.getId());
                    dto.setNumber(ticket.getNumber());
                    dto.setAgencyName(ticket.getAgency().getName());
                    dto.setClientName(ticket.getClient().getUser().getName());
                    dto.setDateCreation(ticket.getIssuedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<TicketReserveDto> getAllTicketsReservesAujourdhui() {
        LocalDate aujourdhui = LocalDate.now();
        return ticketRepository.findByIssuedAtBetween(aujourdhui.atStartOfDay(), aujourdhui.plusDays(1).atStartOfDay())
                .stream()
                .map(ticket -> {
                    TicketReserveDto dto = new TicketReserveDto();
                    dto.setId(ticket.getId());
                    dto.setNumber(ticket.getNumber());
                    dto.setAgencyName(ticket.getAgency().getName());
                    dto.setClientName(ticket.getClient().getUser().getName());
                    dto.setStatus(ticket.getStatus().name());
                    dto.setDateCreation(ticket.getIssuedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}