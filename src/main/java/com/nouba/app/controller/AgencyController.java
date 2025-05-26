package com.nouba.app.controller;

import com.nouba.app.dto.*;
import com.nouba.app.entities.User;
import com.nouba.app.services.AgencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agencies")
@RequiredArgsConstructor
public class AgencyController {
    private final AgencyService agencyService;

    @GetMapping
    public ResponseEntity<List<AgencyResponseDTO>> getAllAgencies() {
        return ResponseEntity.ok(agencyService.getAllAgencies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgencyResponseDTO> getAgencyById(@PathVariable Long id) {
        return ResponseEntity.ok(agencyService.getAgencyById(id));
    }

    @GetMapping("/city/{cityId}")
    public ResponseEntity<List<AgencyResponseDTO>> getAgenciesByCity(@PathVariable Long cityId) {
        return ResponseEntity.ok(agencyService.getAgenciesByCity(cityId));
    }

    /**@GetMapping("/{id}/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENCY')")
    public ResponseEntity<AgencyStatsDTO> getAgencyStats(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(agencyService.getAgencyStats(id, currentUser));
    }*/

   /** @GetMapping("/{agencyId}/queue-count")
    public ResponseEntity<Integer> getQueueCount(@PathVariable Long agencyId) {
        return ResponseEntity.ok(agencyService.getQueueCount(agencyId));
    }

    @GetMapping("/{agencyId}/current-number")
    public ResponseEntity<Integer> getCurrentNumber(@PathVariable Long agencyId) {
        return ResponseEntity.ok(agencyService.getCurrentNumber(agencyId));
    }*/
}