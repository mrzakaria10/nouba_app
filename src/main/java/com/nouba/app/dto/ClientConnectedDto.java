package com.nouba.app.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClientConnectedDto {
    private Long id;
    private String name;
    private String email;
    private LocalDateTime lastLogin;
}
