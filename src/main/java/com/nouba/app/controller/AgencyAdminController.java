package com.nouba.app.controller;

import com.nouba.app.dto.*;
import com.nouba.app.entities.Agency;
import com.nouba.app.services.AgencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/agencies")
@RequiredArgsConstructor
public class AgencyAdminController {

    private final AgencyService agencyService;

    @PostMapping("/city/{cityId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<AgencyResponseDTO>> addAgency(
            @RequestBody AgencyCreateDTO agencyDTO,
            @PathVariable Long cityId) {

        AgencyResponseDTO response = agencyService.createAgency(agencyDTO, cityId);
        return ResponseEntity.ok(
                new ApiResponse<>("Agence créée avec succès", HttpStatus.OK.value(), response)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<AgencyResponseDTO>> updateAgency(
            @PathVariable Long id,
            @RequestBody AgencyUpdateDTO updateDTO) {

        AgencyResponseDTO response = agencyService.updateAgency(id, updateDTO);
        return ResponseEntity.ok(
                new ApiResponse<>("Agence mise à jour", HttpStatus.OK.value(), response)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAgency(@PathVariable Long id) {
        agencyService.deleteAgency(id);
        return ResponseEntity.ok(
                new ApiResponse<>("Agence supprimée", HttpStatus.OK.value(), null)
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<List<AgencyResponseDTO>>> getAllAgencies() {
        List<AgencyResponseDTO> agencies = agencyService.getAllAgencies();
        return ResponseEntity.ok(
                new ApiResponse<>("Liste des agences", HttpStatus.OK.value(), agencies)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<AgencyResponseDTO>> getAgency(@PathVariable Long id) {
        AgencyResponseDTO agency = agencyService.getAgencyDetails(id);
        return ResponseEntity.ok(
                new ApiResponse<>("Détails de l'agence", HttpStatus.OK.value(), agency)
        );
    }
}