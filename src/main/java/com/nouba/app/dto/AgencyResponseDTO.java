package com.nouba.app.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AgencyResponseDTO {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String cityName;
    private String email;
    //private String photoUrl; // Assurez-vous que ce champ existe
}