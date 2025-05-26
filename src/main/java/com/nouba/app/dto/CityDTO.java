package com.nouba.app.dto;

import com.nouba.app.entities.City;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class CityDTO {
    private Long id;
    private String name;

    public CityDTO(City city) {
        this.id = city.getId();
        this.name = city.getName();

    }
}