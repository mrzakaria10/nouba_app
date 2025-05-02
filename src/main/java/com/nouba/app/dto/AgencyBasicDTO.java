package com.nouba.app.dto;

import com.nouba.app.entities.Agency;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgencyBasicDTO {
    private Long id;
    private String name;

    public AgencyBasicDTO(Agency agency) {
        this.id = agency.getId();
        this.name = agency.getName();
    }
}
