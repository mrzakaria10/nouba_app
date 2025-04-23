package com.nouba.app.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgencyResponseDTO {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String cityName;
    private String email;
}
