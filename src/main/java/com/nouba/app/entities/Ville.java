package com.nouba.app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ville {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String codePostal;

    @OneToMany(mappedBy = "ville", cascade = CascadeType.ALL)
    private List<Agence> agences;
}