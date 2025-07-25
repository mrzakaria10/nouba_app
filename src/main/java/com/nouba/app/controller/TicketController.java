package com.nouba.app.controller;

import com.nouba.app.dto.*;
import com.nouba.app.entities.*;
import com.nouba.app.repositories.AgencyRepository;
import com.nouba.app.repositories.ClientRepository;
import com.nouba.app.repositories.ServiceRepository;
import com.nouba.app.repositories.TicketRepository;
import com.nouba.app.services.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final ClientRepository clientRepository;
    private final ServiceRepository serviceRepository;
    private final AgencyRepository agencyRepository;
    private final TicketRepository ticketRepository;

    /**
     * Create a new ticket for an agency with service selection
     */
    @PostMapping("/agency/{agencyId}/{clientId}/{serviceId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<TicketDTO>> takeTicket(
            @PathVariable Long agencyId,
            @PathVariable Long clientId,
            @PathVariable Long serviceId,
            @AuthenticationPrincipal User user) {



        try {
            if (user == null || user.getClient() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(null, "Authentication required", 401));
            }


            if (!clientId.equals(user.getClient().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(null, "Client ID doesn't match authenticated user", 403));
            }

            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));

            Ticket ticket = ticketService.generateTicket(agencyId, serviceId, client.getId(), client);

            return ResponseEntity.ok(
                    new ApiResponse<>(TicketDTO.from(ticket), "Ticket created successfully", 200));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "Error creating ticket: " + e.getMessage(), 500));
        }
    }

    /**
     * Get the status of a specific ticket / الحصول على حالة تذكرة محددة
     * @param ticketId ID of the ticket / معرّف التذكرة
     * @param user Authenticated user / المستخدم المصادق عليه
     * @return ResponseEntity containing ticket status / كيان الاستجابة يحتوي على حالة التذكرة
     */
    @GetMapping("/{ticketId}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTicketStatus(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal User user) {

        Map<String, Object> status = ticketService.getTicketStatus(ticketId, user.getId());
        return ResponseEntity.ok(
                new ApiResponse<>(status,
                        "Ticket status retrieved",
                        200));
    }

    /**
     * Get number of people ahead in queue / الحصول على عدد الأشخاص في الطابور قبل التذكرة
     * @param ticketId ID of the ticket / معرّف التذكرة
     * @param user Authenticated user / المستخدم المصادق عليه
     * @return ResponseEntity containing number of people ahead / كيان الاستجابة يحتوي على عدد الأشخاص قبل التذكرة
     */
    @GetMapping("/{ticketId}/ahead")
    public ResponseEntity<ApiResponse<Integer>> getPeopleAhead(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal User user) {

        ticketService.verifyTicketAccess(ticketId, user.getId()); // Verify access / التحقق من الصلاحية
        int peopleAhead = ticketService.getPeopleAhead(ticketId);

        return ResponseEntity.ok(
                new ApiResponse<>(peopleAhead,
                        "Number of people ahead / عدد الأشخاص قبل التذكرة",
                        200));
    }

    /**
     * Serve the next client in queue / خدمة العميل التالي في الطابور
     * @param agencyId ID of the agency / معرّف الوكالة
     * @return ResponseEntity containing served ticket info / كيان الاستجابة يحتوي على معلومات التذكرة المخدومة
     */
    @PutMapping("/agency/{agencyId}/serve")
    @PreAuthorize("hasRole('AGENCY')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> serveNextClient(
            @PathVariable Long agencyId) {

        Optional<Ticket> servedTicketOpt = ticketService.serveNextClient(agencyId);

        return servedTicketOpt.map(ticket -> {
            // Get client name safely
            String clientName = ticket.getClient() != null && ticket.getClient().getUser() != null
                    ? ticket.getClient().getUser().getName()
                    : "Unknown Client";

            Map<String, Object> response = Map.of(
                    "ticketNumber", ticket.getNumber(),
                    "clientName", clientName,
                    "startedAt", ticket.getStartedAt() != null ? ticket.getStartedAt() : LocalDateTime.now()
            );

            return ResponseEntity.ok(
                    new ApiResponse<>(response,
                            "Next client served",
                            200));
        }).orElseGet(() -> ResponseEntity.ok(
                new ApiResponse<>(null,
                        "No clients in queue",
                        200)));
    }

    /**
     * Get current ticket being served / الحصول على التذكرة قيد الخدمة حالياً
     * @param agencyId ID of the agency / معرّف الوكالة
     * @return ResponseEntity containing current ticket info / كيان الاستجابة يحتوي على معلومات التذكرة الحالية
     */
    @GetMapping("/agency/{agencyId}/current")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentTicket(
            @PathVariable Long agencyId) {

        return ticketService.getCurrentTicket(agencyId)
                .map(ticket -> {
                    Map<String, Object> response = Map.of(
                            "ticketNumber", ticket.getNumber(),
                            "clientName", ticket.getClient().getUser().getName(),
                            "status", ticket.getStatus().name(),
                            "startedAt", ticket.getStartedAt()
                    );
                    return ResponseEntity.ok(
                            new ApiResponse<>(response,
                                    "Current ticket / التذكرة الحالية",
                                    200));
                })
                .orElseGet(() -> ResponseEntity.ok(
                        new ApiResponse<>(null,
                                "No tickets being served / لا توجد تذاكر قيد الخدمة",
                                200)));
    }

    @GetMapping("/admin/reservations/today")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TicketReservationDTO>>> getAllTicketsReservedToday() {
        List<TicketReservationDTO> tickets = ticketService.getAllTicketsReservedToday();
        return ResponseEntity.ok(
                new ApiResponse<>(tickets, "Today's ticket reservations retrieved", 200)
        );
    }

    /**
     * Get all pending tickets for an agency / الحصول على جميع التذاكر المعلقة لوكالة
     * @param agencyId ID of the agency / معرّف الوكالة
     * @return ResponseEntity containing list of pending tickets / كيان الاستجابة يحتوي على قائمة التذاكر المعلقة
     */
    @GetMapping("/agency/{agencyId}/pending")
    @PreAuthorize("hasAnyRole('AGENCY')")
    public ResponseEntity<ApiResponse<List<TicketDTO>>> getAllPendingTicketsByAgency(
            @PathVariable Long agencyId) {

        List<Ticket> pendingTickets = ticketService.getAllPendingTicketsByAgency(agencyId);
        List<TicketDTO> ticketDTOs = pendingTickets.stream()
                .map(TicketDTO::from)
                .toList();

        return ResponseEntity.ok(
                new ApiResponse<>(ticketDTOs,
                        "Pending tickets retrieved successfully",
                        200));
    }

    /**
     * Get count of pending tickets for an agency / الحصول على عدد التذاكر المعلقة لوكالة
     * @param agencyId ID of the agency / معرّف الوكالة
     * @return ResponseEntity containing count of pending tickets / كيان الاستجابة يحتوي على عدد التذاكر المعلقة
     */
    @GetMapping("/agency/{agencyId}/pending/count")
    @PreAuthorize("hasAnyRole('AGENCY')")
    public ResponseEntity<ApiResponse<Integer>> getPendingTicketsCountByAgency(
            @PathVariable Long agencyId) {

        int count = ticketService.countPendingTicketsByAgency(agencyId);

        return ResponseEntity.ok(
                new ApiResponse<>(count,
                        "Pending tickets count retrieved successfully",
                        200));
    }


    /**
     * Get services by agency (for client selection)
     */
    @GetMapping("/agency/{agencyId}/services")
    public ResponseEntity<ApiResponse<List<ServiceDTO>>> getAgencyServices(
            @PathVariable Long agencyId) {
        List<Servicee> agencyServices = serviceRepository.findByAgenciesId(agencyId);
        List<ServiceDTO> dtos = agencyServices.stream()
                .map(s -> new ServiceDTO(s.getId(), s.getName(), s.getDescription()))
                .toList();
        return ResponseEntity.ok(new ApiResponse<>(dtos, "Services retrieved", 200));
    }



    /**
     * Cancel ticket
     */
    @PutMapping("/{ticketId}/cancel")
    @PreAuthorize("hasAnyRole('CLIENT', 'AGENCY')")
    public ResponseEntity<ApiResponse<String>> cancelTicket(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal User user) {

        ticketService.cancelTicket(ticketId, user);
        return ResponseEntity.ok(
                new ApiResponse<>(null,
                        "Ticket cancelled successfully",
                        200));
    }

    //RESET TICKET
    @PostMapping("/admin/reset-tickets")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> manualResetTickets() {
        try {
            ticketService.deleteAllTicketsDaily();
            return ResponseEntity.ok(
                    new ApiResponse<>(null, "All tickets deleted successfully", 200)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "Reset failed: " + e.getMessage(), 500));
        }
    }

    /// Agency Ticket History Endpoint

    // Add to TicketController.java
    @GetMapping("/agency/{agencyId}/history")
    @PreAuthorize("hasAnyRole('AGENCY', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<TicketDTO>>> getAgencyTicketHistory(
            @PathVariable Long agencyId,
            @AuthenticationPrincipal User user) {

        // Authorization check for agency users
        if (user.getRole() == Role.AGENCY) {
            Agency userAgency = agencyRepository.findByUser(user)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Agency not found"));

            if (!userAgency.getId().equals(agencyId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(null, "Not authorized to access this agency's history", 403));
            }
        }

        List<TicketDTO> history = ticketService.getAgencyTicketHistory(agencyId);
        return ResponseEntity.ok(
                new ApiResponse<>(history, "Agency ticket history retrieved", 200));
    }

    // Add to TicketController.java

    /**
     * Get count of EN_ATTENTE tickets for agency today
     * @param agencyId ID of the agency
     * @return Count of waiting tickets
     */
    @GetMapping("/agency/{agencyId}/today/en-attente/count")
    @PreAuthorize("hasAnyRole('AGENCY', 'ADMIN')")
    public ResponseEntity<ApiResponse<Integer>> getEnAttenteCountToday(
            @PathVariable Long agencyId,
            @AuthenticationPrincipal User user) {

        // Authorization check
        if (user.getRole() == Role.AGENCY) {
            verifyAgencyAccess(user, agencyId);
        }

        int count = ticketService.getEnAttenteCountToday(agencyId);
        return ResponseEntity.ok(
                new ApiResponse<>(count, "EN_ATTENTE count for today", 200));
    }

    /**
     * Get count of EN_COURS tickets for agency today
     * @param agencyId ID of the agency
     * @return Count of in-progress tickets
     */
    @GetMapping("/agency/{agencyId}/today/en-cours/count")
    @PreAuthorize("hasAnyRole('AGENCY', 'ADMIN')")
    public ResponseEntity<ApiResponse<Integer>> getEnCoursCountToday(
            @PathVariable Long agencyId,
            @AuthenticationPrincipal User user) {

        if (user.getRole() == Role.AGENCY) {
            verifyAgencyAccess(user, agencyId);
        }

        int count = ticketService.getEnCoursCountToday(agencyId);
        return ResponseEntity.ok(
                new ApiResponse<>(count, "EN_COURS count for today", 200));
    }

    /**
     * Get count of ANNULE tickets for agency today
     * @param agencyId ID of the agency
     * @return Count of cancelled tickets
     */
    @GetMapping("/agency/{agencyId}/today/annule/count")
    @PreAuthorize("hasAnyRole('AGENCY', 'ADMIN')")
    public ResponseEntity<ApiResponse<Integer>> getAnnuleCountToday(
            @PathVariable Long agencyId,
            @AuthenticationPrincipal User user) {

        if (user.getRole() == Role.AGENCY) {
            verifyAgencyAccess(user, agencyId);
        }

        int count = ticketService.getAnnuleCountToday(agencyId);
        return ResponseEntity.ok(
                new ApiResponse<>(count, "ANNULE count for today", 200));
    }

    /**
     * Get count of TERMINE tickets for agency today
     * @param agencyId ID of the agency
     * @return Count of completed tickets
     */
    @GetMapping("/agency/{agencyId}/today/termine/count")
    @PreAuthorize("hasAnyRole('AGENCY', 'ADMIN')")
    public ResponseEntity<ApiResponse<Integer>> getTermineCountToday(
            @PathVariable Long agencyId,
            @AuthenticationPrincipal User user) {

        if (user.getRole() == Role.AGENCY) {
            verifyAgencyAccess(user, agencyId);
        }

        int count = ticketService.getTermineCountToday(agencyId);
        return ResponseEntity.ok(
                new ApiResponse<>(count, "TERMINE count for today", 200));
    }

    // Helper method for agency authorization
    private void verifyAgencyAccess(User user, Long agencyId) {
        Agency userAgency = agencyRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Agency not found"));

        if (!userAgency.getId().equals(agencyId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to access this agency's data");
        }
    }

    /**
     * 1. Get all tickets with all statuses by agency ID (Role: AGENCY)
     * Returns: ticket number, service, creation date, position, estimated time, status
     */
    @GetMapping("/agency/{agencyId}/all")
    @PreAuthorize("hasRole('AGENCY')")
    public ResponseEntity<ApiResponse<List<TicketAgencyDto>>> getAllTicketsByAgency(
            @PathVariable Long agencyId,
            @AuthenticationPrincipal User user) {

        // Get the agency associated with the user
        Agency userAgency = agencyRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not associated with an agency"));

        // Verify the requested agency matches the user's agency
        if (!userAgency.getId().equals(agencyId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized access to this agency's data");
        }

        List<TicketAgencyDto> tickets = ticketService.getAllTicketsByAgency(agencyId);
        return ResponseEntity.ok(
                new ApiResponse<>(tickets, "All tickets retrieved", 200));
    }

    /**
     * 2. Change status from EN_ATTENTE to EN_COURS (Role: AGENCY)
     * Returns: ticket number, service, creation date, client name
     */
    @PutMapping("/{ticketId}/start-service")
    @PreAuthorize("hasRole('AGENCY')")
    public ResponseEntity<ApiResponse<TicketServiceDto>> startTicketService(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal User user) {

        try {
            TicketServiceDto response = ticketService.startTicketService(ticketId, user.getId());
            return ResponseEntity.ok(
                    new ApiResponse<>(response, "Service started", 200));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(new ApiResponse<>(null, e.getReason(), e.getStatusCode().value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "Error starting service: " + e.getMessage(), 500));
        }
    }

    /**
     * 3. Change status from EN_ATTENTE to ANNULE (Role: AGENCY or CLIENT)
     * Returns: client name, ticket number
     */
    @PutMapping("/{ticketId}/cancel-pending")
    @PreAuthorize("hasAnyRole('AGENCY')")
    public ResponseEntity<ApiResponse<TicketCancelDto>> cancelPendingTicket(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal User user) {

        try {
            TicketCancelDto response = ticketService.cancelPendingTicket(ticketId, user);
            return ResponseEntity.ok(
                    new ApiResponse<>(response, "Pending ticket canceled", 200));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(new ApiResponse<>(null, e.getReason(), e.getStatusCode().value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "Error canceling ticket: " + e.getMessage(), 500));
        }
    }

    /**
     * 4. Change status from EN_COURS to TERMINE (Role: AGENCY)
     * Returns: ticket number, service, client name
     */
    @PutMapping("/{ticketId}/complete-service")
    public ResponseEntity<ApiResponse<TicketCompleteDto>> completeTicketService(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal User user) {

        TicketCompleteDto response = ticketService.completeTicketService(ticketId, user.getId());
        return ResponseEntity.ok(
                new ApiResponse<>(response, "Service completed", 200));
    }

    /**
     * 5. Change status from EN_COURS to ANNULE (Role: AGENCY)
     * Returns: client name, ticket number
     */
    @PutMapping("/{ticketId}/cancel-active")
    @PreAuthorize("hasRole('AGENCY')")
    public ResponseEntity<ApiResponse<TicketCancelDto>> cancelActiveTicket(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal User user) {

        try {
            TicketCancelDto response = ticketService.cancelActiveTicket(ticketId, user.getId());
            return ResponseEntity.ok(
                    new ApiResponse<>(response, "Active ticket canceled", 200));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(new ApiResponse<>(null, e.getReason(), e.getStatusCode().value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "Error canceling ticket: " + e.getMessage(), 500));
        }
    }

    /**
     * 6. Get all clients for an agency (Role: AGENCY)
     */
    @GetMapping("/agency/{agencyId}/clients")
    @PreAuthorize("hasRole('AGENCY')")
    public ResponseEntity<ApiResponse<List<ClientDto>>> getAgencyClients(
            @PathVariable Long agencyId,
            @AuthenticationPrincipal User user) {

        // Get the agency associated with the user
        Agency userAgency = agencyRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not associated with an agency"));

        // Verify the requested agency matches the user's agency
        if (!userAgency.getId().equals(agencyId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized access to this agency's data");
        }

        List<ClientDto> clients = ticketService.getAgencyClients(agencyId);
        return ResponseEntity.ok(
                new ApiResponse<>(clients, "Agency clients retrieved", 200));
    }

    /**
     * Get all services for an agency (Role: AGENCY)
     * Returns: List of services with id, name, description
     */
    @GetMapping("/agency/{agencyId}/services-list")
    @PreAuthorize("hasRole('AGENCY')")
    public ResponseEntity<ApiResponse<List<ServiceDTO>>> getAgencyServicesList(
            @PathVariable Long agencyId,
            @AuthenticationPrincipal User user) {

        // Verify the agency belongs to the authenticated user
        Agency userAgency = agencyRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not associated with an agency"));

        if (!userAgency.getId().equals(agencyId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized access to this agency's services");
        }

        List<Servicee> agencyServices = serviceRepository.findByAgenciesId(agencyId);
        List<ServiceDTO> dtos = agencyServices.stream()
                .map(s -> new ServiceDTO(s.getId(), s.getName(), s.getDescription()))
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(dtos, "Services retrieved", 200));
    }

    //new api zakaria ticket

    /**
     * Get next available ticket number for an agency
     */
    @GetMapping("/agency/{agencyId}/next-number")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<String>> getNextTicketNumber(
            @PathVariable Long agencyId) {
        try {
            String nextNumber = ticketService.getNextTicketNumber(agencyId);
            return ResponseEntity.ok(
                    new ApiResponse<>(nextNumber, "Next ticket number retrieved", 200));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "Error getting next ticket number: " + e.getMessage(), 500));
        }
    }

    /**
     * Create ticket with specific number (if available)
     */
    @PostMapping("/agency/{agencyId}/{clientId}/{serviceId}/{ticketNumber}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<TicketDTO>> takeTicketWithNumber(
            @PathVariable Long agencyId,
            @PathVariable Long clientId,
            @PathVariable Long serviceId,
            @PathVariable String ticketNumber,
            @AuthenticationPrincipal User user) {
        try {
            if (user == null || user.getClient() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(null, "Authentication required", 401));
            }

            if (!clientId.equals(user.getClient().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(null, "Client ID doesn't match authenticated user", 403));
            }

            // Validate ticket number format
            if (!ticketNumber.matches("^NOUBA\\d{3}$")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(null, "Invalid ticket number format. Must be like 'nouba001'", 400));
            }

            Ticket ticket = ticketService.generateTicketWithNumber(agencyId, serviceId, clientId, user.getClient(), ticketNumber);

            return ResponseEntity.ok(
                    new ApiResponse<>(TicketDTO.from(ticket), "Ticket created successfully", 200));

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(new ApiResponse<>(null, e.getReason(), e.getStatusCode().value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "Error creating ticket: " + e.getMessage(), 500));
        }
    }



    // NEW API

    /**
     * Get last ticket in EN_ATTENTE status for an agency
     * Obtenir le dernier ticket en statut EN_ATTENTE pour une agence
     * الحصول على آخر تذكرة في حالة انتظار لوكالة
     */
    @GetMapping("/agency/{agencyId}/last-pending")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<TicketDTO>> getLastPendingTicket(
            @PathVariable Long agencyId,
            @AuthenticationPrincipal User user) {

        try {
            Optional<Ticket> lastPendingTicket = ticketService.getLastPendingTicket(agencyId, user.getId());

            return lastPendingTicket.map(ticket ->
                            ResponseEntity.ok(
                                    new ApiResponse<>(TicketDTO.from(ticket),
                                            "Last pending ticket retrieved",
                                            200)))
                    .orElseGet(() -> ResponseEntity.ok(
                            new ApiResponse<>(null,
                                    "No pending tickets found",
                                    200)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "Error retrieving ticket: " + e.getMessage(), 500));
        }
    }

    // start first panding

    @PutMapping("/agency/{agencyId}/start-first-pending")
    @PreAuthorize("hasRole('AGENCY')")
    public ResponseEntity<ApiResponse<TicketProcessingDto>> startFirstPendingTicket(
            @PathVariable Long agencyId,
            @AuthenticationPrincipal User user) {

        try {
            TicketProcessingDto response = ticketService.startFirstPendingTicket(agencyId, user.getId());
            return ResponseEntity.ok(
                    new ApiResponse<>(response, "First pending ticket started successfully", 200));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(new ApiResponse<>(null, e.getReason(), e.getStatusCode().value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "Error starting ticket: " + e.getMessage(), 500));
        }
    }


}