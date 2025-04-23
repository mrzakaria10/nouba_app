package com.nouba.app.repositories;


import com.nouba.app.entities.City;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CityRepository extends JpaRepository<City, Long> {

}
