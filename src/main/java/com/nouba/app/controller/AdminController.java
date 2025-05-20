package com.nouba.app.controller;

import com.nouba.app.dto.AdminSummaryDTO;
import com.nouba.app.dto.ApiResponse;
import com.nouba.app.services.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    /**
     * Retrieves summary statistics for admin dashboard
     * @return ResponseEntity containing counts of agencies, clients, and pending tickets
     */
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminSummaryDTO>> getAdminSummary() {
        AdminSummaryDTO summary = adminService.getAdminSummary();
        return ResponseEntity.ok(new ApiResponse<>(
                summary,
                "Admin summary retrieved successfully",
                200));
    }
}