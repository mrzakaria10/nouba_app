package com.nouba.app.dto;

import lombok.Data;


@Data
public class AgencyActiveDto {
    private Long id;
    private String name;
    private String city;
    private int ticketsToday;
}

