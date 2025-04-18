package com.nouba.app.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
<<<<<<< HEAD
public class Agency {
=======
<<<<<<<< HEAD:src/main/java/com/nouba/app/entities/City.java
public class City {
========
public class Agency {
>>>>>>>> origin/master:src/main/java/com/nouba/app/entities/Agency.java
>>>>>>> origin/master

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
<<<<<<< HEAD
=======
<<<<<<<< HEAD:src/main/java/com/nouba/app/entities/City.java
    private String name;
}
========
>>>>>>> origin/master

    private String name;

    private String address;
<<<<<<< HEAD
    private String phone;
=======
>>>>>>> origin/master

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private City city;
<<<<<<< HEAD

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}
=======
}
>>>>>>>> origin/master:src/main/java/com/nouba/app/entities/Agency.java
>>>>>>> origin/master
