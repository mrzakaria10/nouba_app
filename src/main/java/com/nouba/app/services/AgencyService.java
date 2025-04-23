package com.nouba.app.services;

import com.nouba.app.dto.AgencyUpdateDTO;
import com.nouba.app.entities.Agency;
import com.nouba.app.entities.City;
import com.nouba.app.entities.User;
import com.nouba.app.repositories.AgencyRepository;
import com.nouba.app.repositories.CityRepository;
import com.nouba.app.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgencyService {
    private final AgencyRepository agencyRepository;
    private final CityRepository cityRepository;
    private final UserRepository userRepository;

    @Transactional
    public Agency addAgency(Agency agency, Long cityId) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new RuntimeException("City not found"));
        agency.setCity(city);
        return agencyRepository.save(agency);
    }


    //
    @Transactional
    public Agency updateAgency(Long agencyId, AgencyUpdateDTO updateDTO, Long cityId) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new RuntimeException("Agency not found"));

        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new RuntimeException("City not found"));

        // Update agency fields
        agency.setName(updateDTO.getName());
        agency.setAddress(updateDTO.getAddress());
        agency.setPhone(updateDTO.getPhone());
        agency.setCity(city);

        // Update user through agency
        if (updateDTO.getUserEmail() != null) {
            agency.getUser().setEmail(updateDTO.getUserEmail());
        }
        if (updateDTO.getUserName() != null) {
            agency.getUser().setName(updateDTO.getUserName());
        }

        return agencyRepository.save(agency);
    }
    ///
    // Dans AgencyService.java
    @Transactional
    public void deleteAgency(Long id) {
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agence non trouvée"));

        // Supprimer d'abord l'agence
        agencyRepository.delete(agency);

        // Puis supprimer l'utilisateur associé
        userRepository.delete(agency.getUser());
    }

    /*@Transactional
    public void deleteAgency(Long id) {
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agency not found"));

        User user = agency.getUser();
        agencyRepository.delete(agency);
        userRepository.delete(user);
    }*/

    public List<Agency> getAgenciesByCityId(Long cityId) {
        return agencyRepository.findByCityId(cityId);
    }

    public Agency getAgencyById(Long id) {
        return agencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agency not found"));
    }
}