package com.nouba.app.dto;

import lombok.Data;

@Data
public class ServiceDTO {
    private Long id;
    private String name;
    private String description;

    // Add this constructor
    public ServiceDTO(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}