package com.nouba.app.controller;

import com.nouba.app.dto.AgencyBasicDTO;
import com.nouba.app.dto.ApiResponse;
import com.nouba.app.dto.CityDTO;
import com.nouba.app.entities.Agency;
import com.nouba.app.entities.City;
import com.nouba.app.repositories.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityRepository cityRepository;

    /**
     * Récupère toutes les villes disponibles
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CityDTO>>> getAllCities() {
        List<City> cities = cityRepository.findAll();
        List<CityDTO> dtos = cities.stream()
                .map(CityDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(
                new ApiResponse<>(dtos, "Liste des villes récupérée avec succès", 200));
    }

    /**
     * Récupère les agences d'une ville spécifique
     */
    @GetMapping("/{cityId}/agencies")
    public ResponseEntity<ApiResponse<List<AgencyBasicDTO>>> getAgenciesByCityId(@PathVariable Long cityId) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new RuntimeException("Ville non trouvée"));

        List<AgencyBasicDTO> agencies = city.getAgencies().stream()
                .map(AgencyBasicDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                new ApiResponse<>(agencies, "Agences de la ville récupérées", 200));
    }
}