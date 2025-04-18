package com.nouba.app.controller;

import com.nouba.app.dto.ApiResponse;
import com.nouba.app.entities.Agency;
import com.nouba.app.services.AgencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/agencies")
@RequiredArgsConstructor
public class AgencyController {

    private final AgencyService agencyService;

    @PostMapping("/city/{cityId}")
    public ResponseEntity<ApiResponse<String>> addAgency(@RequestBody Agency agency, @PathVariable Long cityId) {
        agencyService.addAgency(agency, cityId);
        return ResponseEntity.ok(new ApiResponse<>("Agency added successfully", HttpStatus.OK.value()));
    }

    @PutMapping("/{id}/city/{cityId}")
    public ResponseEntity<ApiResponse<String>> updateAgency(@PathVariable Long id,
                                                            @RequestBody Agency agency,
                                                            @PathVariable Long cityId) {
        agencyService.updateAgency(id, agency, cityId);
        return ResponseEntity.ok(new ApiResponse<>("Agency updated successfully", HttpStatus.OK.value()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteAgency(@PathVariable Long id) {
        agencyService.deleteAgency(id);
        return ResponseEntity.ok(new ApiResponse<>("Agency deleted successfully", HttpStatus.OK.value()));
    }

    @GetMapping("/city/{cityId}")
    public List<Agency> getAgenciesByCity(@PathVariable Long cityId) {
        return agencyService.getAgenciesByCityId(cityId);
    }

    @GetMapping("/{id}")
    public Agency getAgency(@PathVariable Long id) {
        return agencyService.getAgencyById(id);
    }
}
