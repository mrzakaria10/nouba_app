package com.nouba.app.services;

import com.nouba.app.dto.TicketPublicDto;
import com.nouba.app.entities.Ticket;
import com.nouba.app.repositories.AgencyRepository;
import com.nouba.app.repositories.CityRepository;
import com.nouba.app.repositories.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublicTicketService {

    private final TicketRepository ticketRepository;
    private final AgencyRepository agencyRepository;
    private final CityRepository cityRepository;

    public TicketPublicDto verifyTicket(String ticketNumber, String city, String agencyName) {
        // Validate ticket number format
        if (!ticketNumber.matches("NOUBA\\d{3}")) {
            throw new IllegalArgumentException("Invalid ticket number format");
        }

        // Find ticket
        Ticket ticket = ticketRepository.findByNumber(ticketNumber)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // Validate city if provided
        if (city != null) {
            cityRepository.findByNameIgnoreCase(city)
                    .orElseThrow(() -> new RuntimeException("Specified city not found"));

            if (!city.equalsIgnoreCase(ticket.getAgency().getCity().getName())) {
                throw new RuntimeException("Ticket not found in specified city");
            }
        }

        // Validate agency if provided
        if (agencyName != null) {
            agencyRepository.findByNameIgnoreCase(agencyName)
                    .orElseThrow(() -> new RuntimeException("Specified agency not found"));

            if (!agencyName.equalsIgnoreCase(ticket.getAgency().getName())) {
                throw new RuntimeException("Ticket not found for specified agency");
            }
        }

        // Calculate position in queue
        int position = ticketRepository.countByAgencyIdAndSequenceLessThanAndPending(
                ticket.getAgency().getId(),
                Integer.parseInt(ticket.getNumber().substring(5)));

        // Prepare response
        return TicketPublicDto.builder()
                .ticketNumber(ticket.getNumber())
                .clientName(ticket.getClient().getUser().getName())
                .agencyName(ticket.getAgency().getName())
                .city(ticket.getAgency().getCity().getName())
                .status(ticket.getStatus().name())
                .issuedAt(ticket.getIssuedAt())
                .positionInQueue(position)
                .estimatedWaitTime(calculateWaitTime(position))
                .build();
    }

    private String calculateWaitTime(int position) {
        int minutes = position * 5;
        if (minutes < 60) {
            return minutes + " minutes";
        }
        return (minutes / 60) + " hours " + (minutes % 60) + " minutes";
    }
}