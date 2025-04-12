package com.nouba.app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Agence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String adresse;

    @OneToMany(mappedBy = "agence", cascade = CascadeType.ALL)
    private List<Ticket> filesActuelles;

    @ManyToOne
    @JoinColumn(name = "ville_id")
    private Ville ville;
}