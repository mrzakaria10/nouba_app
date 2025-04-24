package com.nouba.app.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity // Indique que cette classe est une entité JPA
@Data // Lombok - Génère getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok - Génère un constructeur sans arguments
@AllArgsConstructor // Lombok - Génère un constructeur avec tous les arguments
public class Ticket {

    @Id // Marque ce champ comme identifiant primaire
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incrémentation
    private Long id; // Identifiant unique du ticket

    @Column(nullable = false) // Le numéro ne peut pas être null
    private Integer number; // Numéro du ticket dans la file d'attente

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE") // Valeur par défaut
    private Boolean served = false; // Si le ticket a été servi (isServed existe déjà sous ce nom)

    @Column(nullable = false, updatable = false) // Ne peut pas être modifié après création
    private LocalDateTime issuedAt; // Date/heure de création du ticket

    @Column(nullable = true) // Peut être null tant que le ticket n'est pas servi
    private LocalDateTime servedAt; // Date/heure où le ticket a été servi

    @ManyToOne(optional = false) // Un ticket doit avoir un client
    @JoinColumn(name = "client_id", nullable = false)
    private Client client; // Client associé à ce ticket

    @ManyToOne(optional = false) // Un ticket doit avoir une agence
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency; // Agence où le ticket a été émis

    // Méthode utilitaire pour vérifier si le ticket est servi
    public boolean isServed() {
        return Boolean.TRUE.equals(served); // Null-safe check
    }

    // Méthode pour marquer le ticket comme servi
    public void markAsServed() {
        this.served = true;
        this.servedAt = LocalDateTime.now();
    }
}