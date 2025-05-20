package com.nouba.app.services;

import com.nouba.app.dto.TicketPublicDto;
import com.nouba.app.entities.Ticket;
import com.nouba.app.repositories.AgencyRepository;
import com.nouba.app.repositories.CityRepository;
import com.nouba.app.repositories.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PublicTicketService {

    private final TicketRepository ticketRepository;


    public TicketPublicDto verifyTicket(String ticketNumber, Long cityId, Long agencyId) {
        // Clean and validate ticket number format
        if (ticketNumber != null) {
            ticketNumber = ticketNumber.trim().replaceAll("^\"|\"$", "");
        }

        if (ticketNumber == null || !ticketNumber.matches("(?i)NOUBA\\d{3}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid ticket number format. Expected: NOUBA followed by 3 digits");
        }

        // Find ticket scoped to specific agency and city
        Ticket ticket = ticketRepository.findByNumberAndAgencyAndCity(ticketNumber, agencyId, cityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Ticket not found in specified agency/city"));

        // Rest of your existing validation...
        if (!cityId.equals(ticket.getAgency().getCity().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ticket city mismatch");
        }

        if (!agencyId.equals(ticket.getAgency().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ticket agency mismatch");
        }



        // Calculate position in queue
        int position = ticketRepository.countByAgencyIdAndSequenceLessThanAndPending(
                agencyId,
                Integer.parseInt(ticket.getNumber().substring(5)));  // Added missing parenthesis

        // Prepare response
        return TicketPublicDto.builder()
                .ticketNumber(ticket.getNumber())
                .clientName(ticket.getClient().getUser().getName())
                .clientEmail(ticket.getClient().getUser().getEmail())
                .agencyName(ticket.getAgency().getName())
                .city(ticket.getAgency().getCity().getName())
                .status(ticket.getStatus().name())
                .issuedAt(ticket.getIssuedAt())
                .positionInQueue(position)
                .estimatedWaitTime(calculateWaitTime(position))
                .build();
    }

    private String calculateWaitTime(int position) {
        if (position <= 0) return "0 minutes";

        int minutes = position * 5;
        if (minutes < 60) {
            return minutes + " minutes";
        }
        return (minutes / 60) + " hours " + (minutes % 60) + " minutes";
    }
}