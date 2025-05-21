package com.nouba.app.controller;

import com.nouba.app.dto.ApiResponse;
import com.nouba.app.dto.TicketDTO;
import com.nouba.app.dto.TicketReservationDTO;
import com.nouba.app.entities.*;
import com.nouba.app.repositories.ClientRepository;
import com.nouba.app.services.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    /**
     * Create a new ticket for an agency / إنشاء تذكرة جديدة لوكالة
     * @param agencyId ID of the agency / معرّف الوكالة
     * @param user Authenticated user / المستخدم المصادق عليه
     * @return ResponseEntity containing the created ticket / كيان الاستجابة يحتوي على التذكرة المنشأة
     */
    @PostMapping("/agency/{agencyId}/{clientId}")
    public ResponseEntity<ApiResponse<TicketDTO>> takeTicket(
            @PathVariable Long agencyId,
            @PathVariable Long clientId,
            @AuthenticationPrincipal User user) {




        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));



        Ticket ticket = ticketService.generateTicket(agencyId, clientId, client);
        return ResponseEntity.ok(
                new ApiResponse<>(TicketDTO.from(ticket),
                        "Ticket created successfully",
                        200));
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
    public ResponseEntity<ApiResponse<Map<String, Object>>> serveNextClient(            @PathVariable Long agencyId) {

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
}