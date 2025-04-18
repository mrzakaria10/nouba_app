package com.nouba.app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer number; // Position in the queue

    private Boolean served = false;

    private LocalDateTime issuedAt;

    private LocalDateTime servedAt;

    @ManyToOne
    private Client client;

    @ManyToOne
    private Agency agency;


}