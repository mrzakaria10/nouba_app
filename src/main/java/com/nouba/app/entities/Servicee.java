package com.nouba.app.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Set;

@Entity
@Table(name = "services")
@Data
public class Servicee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @ManyToMany
    @JoinTable(
            name = "service_agency"
            , joinColumns = @JoinColumn(name = "service_id")
            , inverseJoinColumns = @JoinColumn(name = "agency_id")

    )
    private Set<Agency> agencies;

    @OneToMany(mappedBy = "agencyService")
    private Set<Ticket> tickets;
}