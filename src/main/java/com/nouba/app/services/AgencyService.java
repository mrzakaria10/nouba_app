package com.nouba.app.services;

import com.nouba.app.entities.Agency;
import com.nouba.app.entities.City;
import com.nouba.app.entities.User;
import com.nouba.app.repositories.AgencyRepository;
import com.nouba.app.repositories.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgencyService {
    private final AgencyRepository agencyRepository;
    private final CityRepository cityRepository;

    public Agency addAgency(Agency agency, Long cityId) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new RuntimeException("City not found"));
        agency.setCity(city);
        return agencyRepository.save(agency);
    }

    public Agency updateAgency(Long agencyId, Agency updatedAgency, Long cityId) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new RuntimeException("Agency not found"));
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new RuntimeException("City not found"));

        agency.setName(updatedAgency.getName());
        agency.setAddress(updatedAgency.getAddress());
        agency.setCity(city);

        return agencyRepository.save(agency);
    }

    public void deleteAgency(Long id) {
        agencyRepository.deleteById(id);
    }

    public List<Agency> getAgenciesByCityId(Long cityId) {
        return agencyRepository.findByCityId(cityId);
    }

    public Agency getAgencyById(Long id) {
        return agencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agency not found"));
    }
}
