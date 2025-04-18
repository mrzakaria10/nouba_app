package com.nouba.app.repositories;



import com.nouba.app.entities.Client;
import com.nouba.app.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByUser(User user);
}