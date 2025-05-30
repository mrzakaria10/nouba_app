package com.nouba.app.services;

import com.nouba.app.dto.*;
import com.nouba.app.entities.*;
import com.nouba.app.exceptions.TicketNotFoundException;
import com.nouba.app.repositories.AgencyRepository;
import com.nouba.app.repositories.ClientRepository;
import com.nouba.app.repositories.ServiceRepository;
import com.nouba.app.repositories.TicketRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.nouba.app.entities.Servicee;
import org.springframework.web.server.ResponseStatusException;

@Service  // Added parentheses
@RequiredArgsConstructor
public class TicketService {
    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    private final AgencyRepository agencyRepository;
    private final TicketRepository ticketRepository;
    private final EmailService emailService;
    private final ServiceRepository serviceRepository;
    private final ClientRepository clientRepository;




    /**
     * Generate a new ticket for an agency and client
     * Génère un nouveau ticket pour une agence et un client
     * إنشاء تذكرة جديدة لوكالة وعميل
     *
     * @param agencyId ID of the agency / ID de l'agence / معرّف الوكالة
     * @param client Client object / Objet client / كائن العميل
     * @return Created ticket / Ticket créé / التذكرة المنشأة
     */
    @Transactional
    public Ticket generateTicket(Long agencyId, Long serviceId, Long clientId, Client client) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new RuntimeException("Agency not found"));

        Servicee service = serviceRepository.findByIdAndAgenciesId(serviceId, agencyId)
                .orElseThrow(() -> new RuntimeException("Service not available for this agency"));

        if (!client.getId().equals(clientId)) {
            throw new RuntimeException("Client ID mismatch");
        }

        for (int attempt = 0; attempt < 3; attempt++) {
            Integer lastSequence = ticketRepository.findMaxSequenceByAgency(agencyId).orElse(0);
            int nextSequence = lastSequence + 1;

            Ticket ticket = new Ticket();
            ticket.setAgency(agency);
            ticket.setAgencyService(service);
            ticket.setClient(client);
            ticket.setSequenceNumber(nextSequence);
            ticket.setNumber(Ticket.generateTicketNumber(nextSequence));
            ticket.setIssuedAt(LocalDateTime.now());
            ticket.setStatus(Ticket.TicketStatus.EN_ATTENTE);

            try {
                Ticket savedTicket = ticketRepository.save(ticket);
                sendTicketNotification(savedTicket);
                return savedTicket;
            } catch (DataIntegrityViolationException e) {
                if (e.getMessage().contains("unique_agency_number")) {
                    // Log and retry
                    System.out.println("Retrying ticket generation due to duplicate number...");
                    continue;
                } else {
                    throw new RuntimeException("Failed to create ticket", e);
                }
            }
        }

        throw new RuntimeException("Failed to create ticket after 3 attempts");
    }




    private int getNextSequenceNumber(Long agencyId) {
        // Try optimistic approach first
        Optional<Integer> lastSequence = ticketRepository.findMaxSequenceByAgency(agencyId);
        if (lastSequence.isPresent()) {
            return lastSequence.get() + 1;
        }

        // Fallback to pessimistic locking if needed
        List<Ticket> lastTickets = ticketRepository.findLastTicketForAgency(
                agencyId,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "sequenceNumber"))
        );

        if (!lastTickets.isEmpty()) {
            return lastTickets.get(0).getSequenceNumber() + 1;
        }

        return 1; // First ticket for this agency
    }


    private void sendCancellationNotification(Ticket ticket) {
        try {
            Map<String, String> values = new HashMap<>();
            values.put("clientName", ticket.getClient().getUser().getName());
            values.put("ticketNumber", ticket.getNumber());
            values.put("agencyName", ticket.getAgency().getName());

            String content = emailService.loadEmailTemplate("templates.email/ticket-cancellation.html", values);
            emailService.sendEmail(
                    ticket.getClient().getUser().getEmail(),
                    "Ticket cancellation confirmation",
                    content
            );
        } catch (Exception e) {
            logger.error("Error sending cancellation notification", e);
        }
    }

    @Transactional
    public void cancelTicket(Long ticketId, User user) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // Authorization checks
        if (user.getRole() == Role.CLIENT &&
                !ticket.getClient().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to cancel this ticket");
        }

        if (user.getRole() == Role.AGENCY) {
            Agency agency = agencyRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Agency not found"));
            if (!ticket.getAgency().getId().equals(agency.getId())) {
                throw new RuntimeException("Unauthorized to cancel this ticket");
            }
        }

        ticket.cancel();
        ticketRepository.save(ticket);
        sendCancellationNotification(ticket);
    }
    /**
     * Get number of people ahead in queue
     * Obtenir le nombre de personnes devant dans la file d'attente
     * الحصول على عدد الأشخاص قبل التذكرة في الطابور
     *
     * @param ticketId Ticket ID / ID du ticket / معرّف التذكرة
     * @return Number of people ahead / Nombre de personnes devant / عدد الأشخاص قبل التذكرة
     */
    public int getPeopleAhead(Long ticketId) {
        // Find ticket by ID / Trouver le ticket par ID / العثور على التذكرة بواسطة المعرف
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException(
                        "Ticket not found / Ticket non trouvé / التذكرة غير موجودة"
                ));

        // Extract sequence number from ticket number / Extraire le numéro séquentiel du numéro de ticket / استخراج الرقم التسلسلي من رقم التذكرة
        int sequence = Integer.parseInt(ticket.getNumber().substring(5));
        return ticketRepository.countByAgencyIdAndSequenceLessThanAndPending(
                ticket.getAgency().getId(), sequence);
    }

    /**
     * Serve the next client in queue
     * Servir le prochain client dans la file d'attente
     * خدمة العميل التالي في الطابور
     *
     * @param agencyId Agency ID / ID de l'agence / معرّف الوكالة
     * @return Optional of served ticket / Optionnel du ticket servi / اختياري للتذكرة المخدومة
     */
    @Transactional
    public Optional<Ticket> serveNextClient(Long agencyId) {
        // Complete current serving ticket if exists / Compléter le ticket en cours de agencyService s'il existe / إكمال التذكرة الحالية إذا كانت موجودة
        ticketRepository.findCurrentlyServingByAgencyId(agencyId)
                .ifPresent(ticket -> {
                    ticket.completeProcessing();
                    ticketRepository.save(ticket);
                });

        // Get next pending ticket / Obtenir le prochain ticket en attente / الحصول على التذكرة المعلقة التالية
        Optional<Ticket> nextTicketOpt = ticketRepository.findNextPendingByAgencyId(agencyId);
        if (nextTicketOpt.isPresent()) {
            Ticket nextTicket = nextTicketOpt.get();
            nextTicket.startProcessing();
            return Optional.of(ticketRepository.save(nextTicket));
        }
        return Optional.empty();
    }

    /**
     * Get complete ticket status
     * Obtenir le statut complet d'un ticket
     * الحصول على الحالة الكاملة للتذكرة
     *
     * @param ticketId Ticket ID / ID du ticket / معرّف التذكرة
     * @param userId User ID / ID de l'utilisateur / معرّف المستخدم
     * @return Map containing status information / Carte contenant les informations de statut / خريطة تحتوي على معلومات الحالة
     */
    public Map<String, Object> getTicketStatus(Long ticketId, Long userId) {
        // Find ticket by ID and user ID / Trouver le ticket par ID et ID utilisateur / العثور على التذكرة بواسطة المعرف ومعرف المستخدم
        Ticket ticket = ticketRepository.findByIdAndClientUserId(ticketId, userId)
                .orElseThrow(() -> new TicketNotFoundException(
                        "Ticket not found or unauthorized / Ticket non trouvé ou non autorisé / التذكرة غير موجودة أو غير مصرح بها"
                ));

        int peopleAhead = getPeopleAhead(ticketId);
        int estimatedWaitMinutes = calculateWaitTime(ticket.getAgency().getId());

        Map<String, Object> status = new HashMap<>();
        status.put("ticketNumber", ticket.getNumber());
        status.put("agencyName", ticket.getAgency().getName());
        status.put("peopleAhead", peopleAhead);
        status.put("status", ticket.getStatus().name());
        status.put("estimatedWaitMinutes", estimatedWaitMinutes);
        status.put("currentPosition", peopleAhead + 1);

        return status;
    }

    /**
     * Calculate estimated wait time
     * Calculer le temps d'attente estimé
     * حساب وقت الانتظار المتوقع
     *
     * @param agencyId Agency ID / ID de l'agence / معرّف الوكالة
     * @return Estimated wait time in minutes / Temps d'attente estimé en minutes / وقت الانتظار المتوقع بالدقائق
     */
    public int calculateWaitTime(Long agencyId) {
        int peopleInQueue = ticketRepository.countPendingByAgencyId(agencyId);
        return peopleInQueue * 5; // 5 minutes per person / 5 minutes par personne / 5 دقائق لكل شخص
    }

    /**
     * Get current ticket being served
     * Obtenir le ticket actuellement en cours de agencyService
     * الحصول على التذكرة قيد الخدمة حالياً
     *
     * @param agencyId Agency ID / ID de l'agence / معرّف الوكالة
     * @return Optional of current ticket / Optionnel du ticket actuel / اختياري للتذكرة الحالية
     */
    public Optional<Ticket> getCurrentTicket(Long agencyId) {
        return ticketRepository.findCurrentlyServingByAgencyId(agencyId);
    }

    /**
     * Get next ticket in queue
     * Obtenir le prochain ticket dans la file d'attente
     * الحصول على التذكرة التالية في الطابور
     *
     * @param agencyId Agency ID / ID de l'agence / معرّف الوكالة
     * @return Optional of next ticket / Optionnel du prochain ticket / اختياري للتذكرة التالية
     */
    public Optional<Ticket> getNextTicket(Long agencyId) {
        return ticketRepository.findNextPendingByAgencyId(agencyId);
    }

    /**
     * Create ticket with "en attente" status and send notification
     * Créer un ticket avec statut "en attente" et envoyer une notification
     * إنشاء تذكرة بحالة "في انتظار" وإرسال إشعار
     *
     * @param agencyId Agency ID / ID de l'agence / معرّف الوكالة
     * @param client Client object / Objet client / كائن العميل
     * @return Created ticket / Ticket créé / التذكرة المنشأة
     */
    @Transactional
    public Ticket createTicketWithStatusPending(Long agencyId, Long serviceId, Long clientId, Client client) {
        Ticket ticket = generateTicket(agencyId, serviceId, clientId, client);
        sendTicketNotification(ticket);
        return ticket;
    }

    /**
     * Send ticket creation notification
     * Envoyer une notification de création de ticket
     * إرسال إشعار إنشاء التذكرة
     *
     * @param ticket Ticket object / Objet ticket / كائن التذكرة
     */
    private void sendTicketNotification(Ticket ticket) {
        try {
            Map<String, String> values = new HashMap<>();
            values.put("clientName", ticket.getClient().getUser().getName());
            values.put("ticketNumber", ticket.getNumber());
            values.put("agencyName", ticket.getAgency().getName());
            values.put("peopleAhead", String.valueOf(getPeopleAhead(ticket.getId())));
            values.put("estimatedWait", String.valueOf(calculateWaitTime(ticket.getAgency().getId())));
            values.put("status", ticket.getStatus().name());

            String content = emailService.loadEmailTemplate("templates.email/ticket-notification.html", values);
            emailService.sendEmail(
                    ticket.getClient().getUser().getEmail(),
                    "Votre ticket pour " + ticket.getAgency().getName(),
                    content
            );
        } catch (Exception e) {
            logger.error("Error sending notification / Erreur lors de l'envoi de la notification / خطأ في إرسال الإشعار", e);
        }
    }

    /**
     * Check pending tickets periodically (every 2 minutes)
     * Vérifier les tickets en attente périodiquement (toutes les 2 minutes)
     * التحقق من التذاكر المعلقة بشكل دوري (كل دقيقتين)
     */
    @Scheduled(fixedRate = 120000)
    public void checkPendingTickets() {
        List<Ticket> pendingTickets = ticketRepository.findByStatus(Ticket.TicketStatus.EN_ATTENTE);

        for (Ticket ticket : pendingTickets) {
            int peopleAhead = getPeopleAhead(ticket.getId());
            if (peopleAhead <= 5) {
                sendApproachingNotification(ticket, peopleAhead);
            }
        }
    }

    /**
     * Send notification when turn is approaching
     * Envoyer une notification lorsque le tour approche
     * إرسال إشعار عندما يقترب الدور
     *
     * @param ticket Ticket object / Objet ticket / كائن التذكرة
     * @param peopleAhead Number of people ahead / Nombre de personnes devant / عدد الأشخاص قبل التذكرة
     */
    private void sendApproachingNotification(Ticket ticket, int peopleAhead) {
        try {
            Map<String, String> values = new HashMap<>();
            values.put("clientName", ticket.getClient().getUser().getName());
            values.put("ticketNumber", ticket.getNumber());
            values.put("agencyName", ticket.getAgency().getName());
            values.put("peopleAhead", String.valueOf(peopleAhead));
            values.put("estimatedWait", String.valueOf(peopleAhead * 5));
            values.put("status", ticket.getStatus().name());

            String content = emailService.loadEmailTemplate("templates.email/ticket-approaching.html", values);
            emailService.sendEmail(
                    ticket.getClient().getUser().getEmail(),
                    "Votre tour approche (" + ticket.getAgency().getName() + ")",
                    content
            );
        } catch (Exception e) {
            logger.error("Error sending approaching notification / Erreur lors de l'envoi de la notification d'approche / خطأ في إرسال إشعار الاقتراب", e);
        }
    }

    /**
     * Verify ticket access
     * Vérifier l'accès au ticket
     * التحقق من صلاحية الوصول إلى التذكرة
     *
     * @param ticketId Ticket ID / ID du ticket / معرّف التذكرة
     * @param userId User ID / ID de l'utilisateur / معرّف المستخدم
     */
    public void verifyTicketAccess(Long ticketId, Long userId) {
        if (!ticketRepository.existsByIdAndClientUserId(ticketId, userId)) {
            throw new RuntimeException(
                    "Ticket not found or unauthorized / Ticket non trouvé ou non autorisé / التذكرة غير موجودة أو غير مصرح بها"
            );
        }
    }

    public List<TicketReservationDTO> getAllTicketsReservedToday() {
        return ticketRepository.findAllTicketsReservedToday().stream()
                .map(this::convertToReservationDTO)
                .toList();
    }

    private TicketReservationDTO convertToReservationDTO(Ticket ticket) {
        return TicketReservationDTO.builder()
                .ticketNumber(ticket.getNumber())
                .clientName(ticket.getClient().getUser().getName())
                .clientEmail(ticket.getClient().getUser().getEmail())
                .agencyName(ticket.getAgency().getName())
                .city(ticket.getAgency().getCity().getName())
                .issuedAt(ticket.getIssuedAt())
                .timeAgo(calculateTimeAgo(ticket.getIssuedAt()))
                .build();
    }

    private String calculateTimeAgo(LocalDateTime issuedAt) {
        long minutes = ChronoUnit.MINUTES.between(issuedAt, LocalDateTime.now());
        if (minutes < 60) {
            return minutes + " minutes ago";
        }
        long hours = ChronoUnit.HOURS.between(issuedAt, LocalDateTime.now());
        return hours + " hours ago";
    }

    /**
     * Get all pending tickets for an agency
     * Obtenir tous les tickets en attente pour une agence
     * الحصول على جميع التذاكر المعلقة لوكالة
     *
     * @param agencyId Agency ID / ID de l'agence / معرّف الوكالة
     * @return List of pending tickets / Liste des tickets en attente / قائمة التذاكر المعلقة
     */
    public List<Ticket> getAllPendingTicketsByAgency(Long agencyId) {
        return ticketRepository.findAllPendingByAgencyId(agencyId);
    }

    /**
     * Count pending tickets for an agency
     * Compter les tickets en attente pour une agence
     * حساب التذاكر المعلقة لوكالة
     *
     * @param agencyId Agency ID / ID de l'agence / معرّف الوكالة
     * @return Number of pending tickets / Nombre de tickets en attente / عدد التذاكر المعلقة
     */
    public int countPendingTicketsByAgency(Long agencyId) {
        return ticketRepository.countPendingByAgencyId(agencyId);
    }

    // Add to TicketService.java
    @Transactional
    public void deleteAllTicketsDaily() {
        logger.info("Executing daily ticket table reset at {}", LocalDateTime.now());

        // Delete all tickets regardless of status
        ticketRepository.deleteAllTickets();

        logger.info("All tickets (EN_ATTENTE, EN_COURS, ANNULE, TERMINE) have been deleted");
    }

    // Add to TicketService.java
    @Scheduled(cron = "0 0 7 * * ?", zone = "Your/Timezone") // e.g., "Europe/Paris"
    public void scheduledTicketTableReset() throws MessagingException {
        try {
            deleteAllTicketsDaily();

            // Optional: Send notification email
            emailService.sendEmail(
                    "admin@example.com",
                    "Daily Ticket Reset Completed",
                    "All tickets were deleted at " + LocalDateTime.now()
            );

        } catch (Exception e) {
            logger.error("Error during daily ticket table reset", e);
            emailService.sendEmail(
                    "admin@example.com",
                    "Ticket Reset Failed",
                    "Error during reset: " + e.getMessage()
            );
        }
    }

    // In TicketService.java

    // Backup tickets before deletion
    @Transactional
    public List<Ticket> backupTickets() {
        List<Ticket> allTickets = ticketRepository.findAll();
        // In a real implementation, you would save to a backup table or file
        return allTickets;
    }

    // Restore tickets from backup (example implementation)
    @Transactional
    public void restoreTickets(List<Ticket> tickets) {
        ticketRepository.saveAll(tickets);
    }

    // Add to TicketService.java
    public List<TicketDTO> getAgencyTicketHistory(Long agencyId) {
        List<Ticket> tickets = ticketRepository.findCompletedAndCancelledByAgencyId(agencyId);
        return tickets.stream()
                .map(TicketDTO::from)
                .toList();
    }

    // Add to TicketService.java

    public int getEnAttenteCountToday(Long agencyId) {
        return ticketRepository.countEnAttenteTodayByAgency(agencyId);
    }

    public int getEnCoursCountToday(Long agencyId) {
        return ticketRepository.countEnCoursTodayByAgency(agencyId);
    }

    public int getAnnuleCountToday(Long agencyId) {
        return ticketRepository.countAnnuleTodayByAgency(agencyId);
    }

    public int getTermineCountToday(Long agencyId) {
        return ticketRepository.countTermineTodayByAgency(agencyId);
    }

    // 1. Get all tickets by agency
    public List<TicketAgencyDto> getAllTicketsByAgency(Long agencyId) {
        return ticketRepository.findByAgencyId(agencyId).stream()
                .map(ticket -> new TicketAgencyDto(
                        ticket.getNumber(),
                        ticket.getAgencyService().getName(),
                        ticket.getIssuedAt(),
                        calculatePosition(ticket),
                        calculateWaitTime(ticket),
                        ticket.getStatus().name()
                ))
                .collect(Collectors.toList());
    }

    public TicketServiceDto startTicketService(Long ticketId, Long userId) {
        // First verify the user is from the agency that owns the ticket
        Agency agency = agencyRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not associated with an agency"));

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found"));

        // Verify the ticket belongs to the agency
        if (!ticket.getAgency().getId().equals(agency.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ticket does not belong to your agency");
        }

        if (!ticket.getStatus().equals(Ticket.TicketStatus.EN_ATTENTE)) {
            throw new IllegalStateException("Ticket must be in EN_ATTENTE status");
        }

        ticket.startProcessing();
        ticket = ticketRepository.save(ticket);

        return new TicketServiceDto(
                ticket.getNumber(),
                ticket.getAgencyService().getName(), // Use the service name
                ticket.getIssuedAt(),
                ticket.getClient().getUser().getName()
        );
    }

    // 3. Cancel pending ticket (EN_ATTENTE → ANNULE)
    @Transactional
    public TicketCancelDto cancelPendingTicket(Long ticketId, User user) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found"));

        // For agency users - verify they own the agency
        if (user.getRole().equals(Role.AGENCY)) {
            Agency agency = agencyRepository.findByUser_Id(user.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not associated with an agency"));

            if (!ticket.getAgency().getId().equals(agency.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ticket doesn't belong to your agency");
            }
        }
        // For client users - verify they own the ticket
        else if (user.getRole().equals(Role.CLIENT)) {
            if (!ticket.getClient().getUser().getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ticket doesn't belong to you");
            }
        }

        if (!ticket.getStatus().equals(Ticket.TicketStatus.EN_ATTENTE)) {
            throw new IllegalStateException("Ticket must be in EN_ATTENTE status");
        }

        ticket.setStatus(Ticket.TicketStatus.ANNULE);
        ticket = ticketRepository.save(ticket);

        return new TicketCancelDto(
                ticket.getClient().getUser().getName(),
                ticket.getNumber()
        );
    }
    // 4. Complete service (EN_COURS → TERMINE)
    public TicketCompleteDto completeTicketService(Long ticketId, Long userId) {
        // First get the agency for the current user
        Agency agency = agencyRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not associated with an agency"));

        // Then find the ticket that belongs to this agency
        Ticket ticket = ticketRepository.findByIdAndAgencyId(ticketId, agency.getId())
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found or doesn't belong to your agency"));

        if (!ticket.getStatus().equals(Ticket.TicketStatus.EN_COURS)) {
            throw new IllegalStateException("Ticket must be in EN_COURS status");
        }

        ticket.completeProcessing();
        ticket = ticketRepository.save(ticket);

        return new TicketCompleteDto(
                ticket.getNumber(),
                ticket.getAgency().getName(),
                ticket.getClient().getUser().getName()
        );
    }
    // 5. Cancel active ticket (EN_COURS → ANNULE)
    @Transactional
    public TicketCancelDto cancelActiveTicket(Long ticketId, Long userId) {
        // First get the agency for the current user
        Agency agency = agencyRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not associated with an agency"));

        // Then find the ticket that belongs to this agency
        Ticket ticket = ticketRepository.findByIdAndAgencyId(ticketId, agency.getId())
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found or doesn't belong to your agency"));

        if (!ticket.getStatus().equals(Ticket.TicketStatus.EN_COURS)) {
            throw new IllegalStateException("Ticket must be in EN_COURS status");
        }

        ticket.setStatus(Ticket.TicketStatus.ANNULE);
        ticket = ticketRepository.save(ticket);

        return new TicketCancelDto(
                ticket.getClient().getUser().getName(),
                ticket.getNumber()
        );
    }
    // 6. Get all clients for agency
    public List<ClientDto> getAgencyClients(Long agencyId) {
        // Option 1: Using JPQL query
        return clientRepository.findClientsByAgencyId(agencyId).stream()
                .map(client -> new ClientDto(
                        client.getUser().getName(),
                        client.getUser().getEmail(),
                        client.getUser().getPhone()
                ))
                .collect(Collectors.toList());

        // Option 2: Alternative implementation
        // return ticketRepository.findByAgencyId(agencyId).stream()
        //         .map(Ticket::getClient)
        //         .distinct()
        //         .map(client -> new ClientDto(
        //                 client.getUser().getName(),
        //                 client.getUser().getEmail(),
        //                 client.getUser().getPhone()
        //         ))
        //         .collect(Collectors.toList());
    }

    // Helper methods
    private int calculatePosition(Ticket ticket) {
        return ticketRepository.countByAgencyIdAndSequenceLessThanAndPending(
                ticket.getAgency().getId(),
                Integer.parseInt(ticket.getNumber().substring(5))
                        + 1);
    }

    private String calculateWaitTime(Ticket ticket) {
        int position = calculatePosition(ticket);
        int minutes = position * 5; // 5 minutes per person
        return minutes < 60 ? minutes + " minutes" :
                (minutes / 60) + " hours " + (minutes % 60) + " minutes";
    }

    // new api zakaria

    /**
     * Get next available ticket number for an agency
     */
    public String getNextTicketNumber(Long agencyId) {
        Integer lastSequence = ticketRepository.findMaxSequenceByAgency(agencyId).orElse(0);
        int nextSequence = lastSequence + 1;
        return String.format("NOUBA%03d", nextSequence);
    }

    /**
     * Generate ticket with specific number (if available)
     */
    @Transactional
    public Ticket generateTicketWithNumber(Long agencyId, Long serviceId, Long clientId, Client client, String ticketNumber) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new RuntimeException("Agency not found"));

        Servicee service = serviceRepository.findByIdAndAgenciesId(serviceId, agencyId)
                .orElseThrow(() -> new RuntimeException("Service not available for this agency"));

        if (!client.getId().equals(clientId)) {
            throw new RuntimeException("Client ID mismatch");
        }

        // Validate ticket number format
        if (!ticketNumber.matches("^NOUBA\\d{3}$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ticket number format");
        }

        // Extract sequence number from ticket number
        int sequenceNumber;
        try {
            sequenceNumber = Integer.parseInt(ticketNumber.substring(5));
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ticket number format");
        }

        // Check if number is already taken
        if (ticketRepository.existsByAgencyIdAndNumber(agencyId, ticketNumber)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This ticket number is already taken");
        }

        // Check if number is the next available
        Integer lastSequence = ticketRepository.findMaxSequenceByAgency(agencyId).orElse(0);
        if (sequenceNumber <= lastSequence) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Please choose a number after " + String.format("NOUBA%03d", lastSequence));
        }

        Ticket ticket = new Ticket();
        ticket.setAgency(agency);
        ticket.setAgencyService(service);
        ticket.setClient(client);
        ticket.setSequenceNumber(sequenceNumber);
        ticket.setNumber(ticketNumber);
        ticket.setIssuedAt(LocalDateTime.now());
        ticket.setStatus(Ticket.TicketStatus.EN_ATTENTE);

        try {
            Ticket savedTicket = ticketRepository.save(ticket);
            sendTicketNotification(savedTicket);
            return savedTicket;
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This ticket number is already taken");
        }
    }

    // In TicketService.java
    /**
     * Get last ticket in EN_ATTENTE status for an agency
     * Obtenir le dernier ticket en statut EN_ATTENTE pour une agence
     * الحصول على آخر تذكرة في حالة انتظار لوكالة
     */
    public Optional<Ticket> getLastPendingTicket(Long agencyId, Long userId) {
        return ticketRepository.findLastPendingTicketForClient(agencyId, userId);
    }

    // new first ticket en attende

    /**
     * Get and start processing the first pending ticket for an agency
     */
    @Transactional
    public TicketProcessingDto startFirstPendingTicket(Long agencyId, Long userId) {
        // Verify user is from the agency
        Agency agency = agencyRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not associated with an agency"));

        if (!agency.getId().equals(agencyId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized for this agency");
        }

        // Get the first pending ticket
        Ticket ticket = ticketRepository.findFirstPendingTicketByAgency(agencyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No pending tickets found"));

        // Verify ticket is still in EN_ATTENTE status
        if (!ticket.getStatus().equals(Ticket.TicketStatus.EN_ATTENTE)) {
            throw new IllegalStateException("Ticket is no longer in EN_ATTENTE status");
        }

        // Update status to EN_COURS
        ticket.startProcessing();
        ticket = ticketRepository.save(ticket);

        return new TicketProcessingDto(
                ticket.getId(),          // Add ticket ID
                ticket.getNumber(),
                ticket.getAgencyService().getName(),
                ticket.getIssuedAt(),
                ticket.getClient().getUser().getName(),
                ticket.getStatus().toString()
        );
    }

    /**
     * Send ticket creation confirmation with all details
     */
    private void sendTicketCreationConfirmation(Ticket ticket) {
        try {
            Map<String, String> values = new HashMap<>();
            values.put("clientName", ticket.getClient().getUser().getName());
            values.put("ticketNumber", ticket.getNumber());
            values.put("agencyName", ticket.getAgency().getName());
            values.put("serviceName", ticket.getAgencyService() != null ?
                    ticket.getAgencyService().getName() : "N/A");
            values.put("peopleAhead", String.valueOf(getPeopleAhead(ticket.getId())));
            values.put("estimatedWait", String.valueOf(calculateWaitTime(ticket.getAgency().getId())));
            values.put("status", ticket.getStatus().name());
            values.put("currentPosition", String.valueOf(getPeopleAhead(ticket.getId()) + 1));

            // Send the detailed creation confirmation email
            String creationContent = emailService.loadEmailTemplate(
                    "templates.emails/ticket-creation-confirmation.html",
                    values
            );

            emailService.sendEmail(
                    ticket.getClient().getUser().getEmail(),
                    "Confirmation de création de ticket - " + ticket.getNumber(),
                    creationContent
            );

            // Also send the regular notification (optional)
            String notificationContent = emailService.loadEmailTemplate(
                    "templates.emails/ticket-notification.html",
                    values
            );

            emailService.sendEmail(
                    ticket.getClient().getUser().getEmail(),
                    "Votre ticket pour " + ticket.getAgency().getName(),
                    notificationContent
            );

        } catch (Exception e) {
            logger.error("Error sending ticket creation confirmation", e);
        }
    }
}
