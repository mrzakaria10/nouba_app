package com.nouba.app.services;

import com.nouba.app.dto.TicketReservationDTO;
import com.nouba.app.entities.*;
import com.nouba.app.entities.AgencyService;
import com.nouba.app.exceptions.TicketNotFoundException;
import com.nouba.app.repositories.AgencyRepository;
import com.nouba.app.repositories.ServiceRepository;
import com.nouba.app.repositories.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service  // Added parentheses
@RequiredArgsConstructor
public class TicketService {
    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    private final AgencyRepository agencyRepository;
    private final TicketRepository ticketRepository;
    private final EmailService emailService;
    private final ServiceRepository serviceRepository;



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

        AgencyService service = serviceRepository.findByIdAndAgenciesId(serviceId, agencyId)
                .orElseThrow(() -> new RuntimeException("AgencyService not available for this agency"));

        Integer lastSequence = ticketRepository.findMaxSequenceByAgency(agencyId)
                .orElse(0);

        Ticket ticket = new Ticket();
        ticket.setAgency(agency);
        ticket.setAgencyService(service);  // Set the agencyService
        ticket.setClient(client);
        ticket.setNumber(Ticket.generateTicketNumber(lastSequence + 1));
        ticket.setIssuedAt(LocalDateTime.now());
        ticket.setStatus(Ticket.TicketStatus.EN_ATTENTE);

        Ticket savedTicket = ticketRepository.save(ticket);
        sendTicketNotification(savedTicket);  // Send notification
        return savedTicket;
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
}
