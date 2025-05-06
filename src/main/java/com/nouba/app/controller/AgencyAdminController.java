package com.nouba.app.controller;

import com.nouba.app.dto.*;
import com.nouba.app.exceptions.auth.UserAlreadyExistsException;
import com.nouba.app.services.AgencyAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/admin/agencies")
@RequiredArgsConstructor
public class AgencyAdminController {
    private final AgencyAdminService agencyAdminService;

    @PostMapping
    public ResponseEntity<ApiResponse<AgencyResponseDTO>> createAgency(
            @Valid @ModelAttribute AgencyCreateDTO dto) throws IOException {
        try {
            AgencyResponseDTO response = agencyAdminService.createAgency(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(
                            response,
                            "Agency created successfully",
                            HttpStatus.CREATED.value()));
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(
                            null,
                            e.getMessage(),
                            HttpStatus.CONFLICT.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(
                            null,
                            "Error creating agency: " + e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AgencyResponseDTO>> updateAgency(
            @PathVariable Long id,
            @ModelAttribute AgencyUpdateDTO dto) throws IOException {
        try {
            AgencyResponseDTO response = agencyAdminService.updateAgency(id, dto);
            return ResponseEntity.ok(new ApiResponse<>(
                    response,
                    "Agency updated successfully",
                    HttpStatus.OK.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(
                            null,
                            "Error updating agency: " + e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAgency(@PathVariable Long id) {
        try {
            agencyAdminService.deleteAgency(id);
            return ResponseEntity.ok(new ApiResponse<>(
                    null,
                    "Agency deleted successfully",
                    HttpStatus.OK.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(
                            null,
                            "Error deleting agency: " + e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}