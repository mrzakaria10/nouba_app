package com.nouba.app.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AgencyResponseDTO {
    private Long id;
    private String name;
    private String photoUrl;
    private String address;
    private String phone;
    private CityBasicDTO city;
    private String email;
}
