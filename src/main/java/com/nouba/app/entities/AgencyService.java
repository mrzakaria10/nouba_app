package com.nouba.app.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Set;

@Entity
@Table(name = "agencyService")
@Data
public class AgencyService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @ManyToMany(mappedBy = "services")
    private Set<Agency> agencies;

    @OneToMany(mappedBy = "agencyService")
    private Set<Ticket> tickets;
}