package com.nouba.app.controller;

import com.nouba.app.dto.ActiveClientDTO;
import com.nouba.app.dto.ApiResponse;
import com.nouba.app.dto.UserBasicInfoDTO;
import com.nouba.app.entities.Role;
import com.nouba.app.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserBasicInfoDTO>>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENCY', 'CLIENT')")
    public ResponseEntity<ApiResponse<?>> getUsersByRole(@PathVariable Role role) {
        // Additional security check for non-admin users
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deleteUser(id));
    }

    // Add to UserController.java
    @GetMapping("/active-this-week")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ActiveClientDTO>>> getActiveClientsThisWeek() {
        return ResponseEntity.ok(userService.getActiveClientsThisWeek());
    }
}