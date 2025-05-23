package com.nouba.app.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"agency_id", "number"})
})

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    // Add to Ticket class
    @ManyToOne
    @JoinColumn(name = "service_id")
    private Servicee agencyService;

    // Update enum
    public enum TicketStatus {
        EN_ATTENTE,
        EN_COURS,
        TERMINE,
        ANNULE
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String number;  // // Format: NOUBA001 Changed to String for formatted number / تم التغيير إلى String لتنسيق الرقم

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.EN_ATTENTE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime issuedAt; // Date de création / تاريخ الإنشاء

    @Column(nullable = true)
    private LocalDateTime startedAt; // Quand le traitement commence / عندما تبدأ المعالجة

    @Column(nullable = true)
    private LocalDateTime completedAt; // Quand le traitement commence / عندما تبدأ المعالجة

    // Add explicit served field to match repository queries
    @Column(nullable = false)
    private boolean served = false;

    @Column(nullable = false)
    private Integer sequenceNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    @JsonIgnoreProperties("tickets")
    private Client client;

    @ManyToOne(optional = false)
    @JoinColumn(name = "agency_id", nullable = false)
    @JsonIgnoreProperties("tickets")
    private Agency agency;



    /**
     * Generate a formatted ticket number combining agency ID and sequence
     * Format: NOUBA + agencyId (3 digits) + sequence (3 digits)
     * Example: NOUBA001001 for agency 1, sequence 1
     */
    public static String generateTicketNumber(int sequence) {
        return String.format("NOUBA%03d", sequence); // Format: NOUBA001
    }

    // Method to update status to "en cours" / طريقة لتحديث الحالة إلى "قيد المعالجة"
    public void startProcessing() {
        this.status = TicketStatus.EN_COURS;
        this.startedAt = LocalDateTime.now();
    }

    // Method to update status to "terminé" / طريقة لتحديث الحالة إلى "مكتمل"
    public void completeProcessing() {
        this.status = TicketStatus.TERMINE;
        this.completedAt = LocalDateTime.now();
    }

    // Add cancel method
    public void cancel() {
        if (this.status != TicketStatus.EN_ATTENTE) {
            throw new IllegalStateException("Only pending tickets can be cancelled");
        }
        this.status = TicketStatus.ANNULE;
        this.completedAt = LocalDateTime.now();
    }
    /**
     * Returns whether the ticket has been served
     * Now uses both the explicit field and status for reliability
     */
    public boolean isServed() {
        return this.served || this.status == TicketStatus.TERMINE;
    }

    /**
     * Helper method to keep served status in sync with ticket status

    @PreUpdate
    @PrePersist
    private void updateServedStatus() {
        this.served = this.status == TicketStatus.TERMINE;
    }
    */


//    @Column(unique = true, nullable = false, updatable = false)
//    private String publicAccessCode; // Example: "NOUBA-001-ABC123"

    /**
     * Combined callback method for all pre-persist operations
     */
    @PrePersist
    public void prePersistOperations() {
        if (this.id == null) {
            // Ensure ID is generated before other operations
            // This is just a safety check - GenerationType.IDENTITY should handle it
        }
    }
}