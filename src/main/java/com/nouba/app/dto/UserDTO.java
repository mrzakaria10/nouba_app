package com.nouba.app.dto;

import com.nouba.app.entities.Role;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private boolean enabled;
    private LocalDateTime lastLogin;

    // Add fromEntity method if needed
}