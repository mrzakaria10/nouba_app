package com.nouba.app.services;

import com.nouba.app.dto.*;
import com.nouba.app.entities.*;
import com.nouba.app.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgencyService {
    private final AgencyRepository agencyRepository;
    private final TicketRepository ticketRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    public List<AgencyResponseDTO> getAllAgencies() {
        return agencyRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AgencyResponseDTO> getAgenciesByCity(Long cityId) {
        return agencyRepository.findByCityId(cityId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public AgencyResponseDTO getAgencyById(Long id) {
        return agencyRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Agency not found"));
    }

    /**public int getQueueCount(Long agencyId) {
        return ticketRepository.countByAgencyIdAndServedFalse(agencyId);
    }

    public String getCurrentNumber(Long agencyId) {
        return ticketRepository.findFirstByAgencyIdAndServedFalseOrderByNumberAsc(agencyId)
                .map(Ticket::getNumber)
                .orElse(null);
    }*/

    private AgencyResponseDTO convertToDTO(Agency agency) {
        return AgencyResponseDTO.builder()
                .id(agency.getId())
                .name(agency.getName())
                .photoUrl(agency.getPhotoUrl())
                .address(agency.getAddress())
                .phone(agency.getPhone())
                .city(CityBasicDTO.builder().name(agency.getCity().getName()).build())
                .email(agency.getUser().getEmail())
                .build();
    }

    public AgencyStatsDTO getAgencyStats(Long agencyId, User currentUser) {
        // Verify if the current user has access to this agency's stats
        if (!hasAccessToAgency(currentUser, agencyId)) {
            throw new AccessDeniedException("You don't have permission to view this agency's statistics");
        }

        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new RuntimeException("Agency not found"));

        int pending = ticketRepository.countByAgencyIdAndStatus(agencyId, Ticket.TicketStatus.EN_ATTENTE);
        int completed = ticketRepository.countByAgencyIdAndStatus(agencyId, Ticket.TicketStatus.TERMINE);
        int inProgress = ticketRepository.countByAgencyIdAndStatus(agencyId, Ticket.TicketStatus.EN_COURS);

        // Count clients associated with this agency through tickets
        int clientCount = clientRepository.countDistinctByTicketsAgencyId(agencyId);

        return AgencyStatsDTO.builder()
                .agencyId(agency.getId())
                .agencyName(agency.getName())
                .totalClients(clientCount)
                .pendingTickets(pending)
                .completedTickets(completed)
                .inProgressTickets(inProgress)
                .build();
    }

    private boolean hasAccessToAgency(User user, Long agencyId) {
        // ADMIN can access any agency
        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        // AGENCY can only access their own agency
        if (user.getRole() == Role.AGENCY) {
            Optional<Agency> agency = agencyRepository.findByUser(user);
            return agency.isPresent() && agency.get().getId().equals(agencyId);
        }

        return false;
    }

}