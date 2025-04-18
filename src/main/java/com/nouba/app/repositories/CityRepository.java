package com.nouba.app.repositories;


import com.nouba.app.entities.City;
import com.nouba.app.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {

}
