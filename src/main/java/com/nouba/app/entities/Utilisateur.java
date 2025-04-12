package com.nouba.app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private String téléphone;

    @Column(nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL)
    private List<Ticket> tickets;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    public class Client extends Utilisateur {
        private String adresse;
        private String cin;
        // other client-specific fields
    }

    // Les méthodes comme réserver ou annuler peuvent être placées dans le service
}
