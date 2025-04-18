package com.nouba.app.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
<<<<<<<< HEAD:src/main/java/com/nouba/app/entities/City.java
public class City {
========
public class Agency {
>>>>>>>> origin/master:src/main/java/com/nouba/app/entities/Agency.java

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
<<<<<<<< HEAD:src/main/java/com/nouba/app/entities/City.java
    private String name;
}
========

    private String name;

    private String address;

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private City city;
}
>>>>>>>> origin/master:src/main/java/com/nouba/app/entities/Agency.java
