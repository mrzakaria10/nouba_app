package com.nouba.app.services;

import com.nouba.app.dto.*;
import com.nouba.app.entities.Agency;
import com.nouba.app.entities.City;
import com.nouba.app.entities.User;
import com.nouba.app.entities.Role;
import com.nouba.app.repositories.AgencyRepository;
import com.nouba.app.repositories.CityRepository;
import com.nouba.app.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgencyService {
    private final AgencyRepository agencyRepository;
    private final CityRepository cityRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AgencyResponseDTO createAgency(AgencyCreateDTO dto, Long cityId) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new RuntimeException("Ville non trouvée"));

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.AGENCY);
        user.setEnabled(true);
        user = userRepository.save(user);

        Agency agency = new Agency();
        agency.setName(dto.getName());
        agency.setAddress(dto.getAddress());
        agency.setPhone(dto.getPhone());
        agency.setCity(city);
        agency.setUser(user);
        agency = agencyRepository.save(agency);

        return mapToResponseDTO(agency);
    }

    @Transactional
    public AgencyResponseDTO updateAgency(Long agencyId, AgencyUpdateDTO updateDTO) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new RuntimeException("Agency not found"));

        // Update city if provided
        if (updateDTO.getCityId() != null) {
            City city = cityRepository.findById(updateDTO.getCityId())
                    .orElseThrow(() -> new RuntimeException("City not found"));
            agency.setCity(city);
        }

        // Update agency fields
        agency.setName(updateDTO.getName());
        agency.setAddress(updateDTO.getAddress());
        agency.setPhone(updateDTO.getPhone());

        // Update user email
        User user = agency.getUser();
        user.setEmail(updateDTO.getEmail());
        userRepository.save(user);

        agency = agencyRepository.save(agency);
        return mapToResponseDTO(agency);
    }

    @Transactional
    public void deleteAgency(Long id) {
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agence non trouvée"));
        User user = agency.getUser();
        agencyRepository.delete(agency);
        userRepository.save(user); // Consider if you really want to delete the user
    }

    public List<AgencyResponseDTO> getAllAgencies() {
        return agencyRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public AgencyResponseDTO getAgencyById(Long id) {
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agency not found"));
        return mapToResponseDTO(agency);
    }

    public List<AgencyResponseDTO> getAgenciesByCityId(Long cityId) {
        return agencyRepository.findByCityId(cityId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private AgencyResponseDTO mapToResponseDTO(Agency agency) {
        return AgencyResponseDTO.builder()
                .id(agency.getId())
                .name(agency.getName())
                .address(agency.getAddress())
                .phone(agency.getPhone())
                .cityName(agency.getCity().getName())
                .email(agency.getUser().getEmail())
                .build();
    }
}