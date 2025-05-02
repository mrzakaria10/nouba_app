package com.nouba.app.controller;

import com.nouba.app.dto.*;
import com.nouba.app.services.AgencyAdminService;
import com.nouba.app.services.AgencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agencies")
@RequiredArgsConstructor
public class AgencyController {

    private final AgencyService agencyService;

    /**
     * Récupère toutes les agences
     * @return Liste des agences
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AgencyResponseDTO>>> getAllAgencies() {
        List<AgencyResponseDTO> agencies = agencyService.getAllAgencies();
        return ResponseEntity.ok(
                new ApiResponse<>(agencies, "Liste des agences récupérée", 200));
    }

    /**
     * Récupère les agences d'une ville spécifique
     * @param cityId ID de la ville
     * @return Liste des agences
     */
    @GetMapping("/city/{cityId}")
    public ResponseEntity<ApiResponse<List<AgencyResponseDTO>>> getAgenciesByCity(@PathVariable Long cityId) {
        List<AgencyResponseDTO> agencies = agencyService.getAgenciesByCity(cityId);
        return ResponseEntity.ok(
                new ApiResponse<>(agencies, "Agences par ville récupérées", 200));
    }

    /**
     * Récupère le nombre de personnes en attente dans une agence
     * @param agencyId ID de l'agence
     * @return Nombre de personnes en attente
     */
    @GetMapping("/{agencyId}/queue-count")
    public ResponseEntity<ApiResponse<Integer>> getQueueCount(@PathVariable Long agencyId) {
        int count = agencyService.getQueueCount(agencyId);
        return ResponseEntity.ok(
                new ApiResponse<>(count, "Nombre de personnes en attente", 200));
    }

    /**
     * Récupère le numéro actuellement servi dans l'agence
     * @param agencyId ID de l'agence
     * @return Numéro actuellement servi
     */
    @GetMapping("/{agencyId}/current-number")
    public ResponseEntity<ApiResponse<Integer>> getCurrentNumber(@PathVariable Long agencyId) {
        Integer currentNumber = agencyService.getCurrentNumber(agencyId);
        return ResponseEntity.ok(
                new ApiResponse<>(currentNumber, "Numéro actuellement servi", 200));
    }
}