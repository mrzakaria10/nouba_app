package com.nouba.app.services;

import com.nouba.app.dto.*;
import com.nouba.app.entities.*;
import com.nouba.app.exceptions.auth.UserAlreadyExistsException;
import com.nouba.app.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class AgencyAdminService {
    private final AgencyRepository agencyRepository;
    private final CityRepository cityRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;

    @Transactional
    public AgencyResponseDTO createAgency(AgencyCreateDTO dto) throws IOException {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }

        City city = cityRepository.findById(dto.getCityId())
                .orElseThrow(() -> new RuntimeException("City not found"));

        String photoUrl = fileStorageService.storeFile(dto.getPhoto());

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.AGENCY);
        user.setEnabled(true);
        user = userRepository.save(user);

        Agency agency = new Agency();
        agency.setName(dto.getName());
        agency.setAddress(dto.getAddress());
        agency.setPhone(dto.getPhone());
        agency.setPhotoUrl(photoUrl);
        agency.setCity(city);
        agency.setUser(user);
        agency = agencyRepository.save(agency);

        emailService.sendAgencyCreationEmail(
                dto.getEmail(),
                dto.getName(),
                dto.getPhone(),
                dto.getAddress(),
                city.getName(),
                dto.getPassword() // Added the missing password parameter
        );

        return convertToDTO(agency);
    }

    @Transactional
    public AgencyResponseDTO updateAgency(Long agencyId, AgencyUpdateDTO updateDTO) throws IOException {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new RuntimeException("Agency not found"));

        if (updateDTO.getCityId() != null) {
            City city = cityRepository.findById(updateDTO.getCityId())
                    .orElseThrow(() -> new RuntimeException("City not found"));
            agency.setCity(city);
        }

        if (updateDTO.getPhoto() != null && !updateDTO.getPhoto().isEmpty()) {
            if (agency.getPhotoUrl() != null) {
                fileStorageService.deleteFile(agency.getPhotoUrl());
            }
            agency.setPhotoUrl(fileStorageService.storeFile(updateDTO.getPhoto()));
        }

        updateIfNotNull(updateDTO.getName(), agency::setName);
        updateIfNotNull(updateDTO.getAddress(), agency::setAddress);
        updateIfNotNull(updateDTO.getPhone(), agency::setPhone);

        User user = agency.getUser();
        updateIfNotNull(updateDTO.getEmail(), user::setEmail);
       updateIfNotNull(updateDTO.getName(), user::setName);
        userRepository.save(user);

        return convertToDTO(agencyRepository.save(agency));
    }

    @Transactional
    public void deleteAgency(Long id) {
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agency not found"));

        if (agency.getPhotoUrl() != null) {
            fileStorageService.deleteFile(agency.getPhotoUrl());
        }

        userRepository.delete(agency.getUser());
        agencyRepository.delete(agency);
    }

    private <T> void updateIfNotNull(T newValue, Consumer<T> setter) {
        if (newValue != null) {
            setter.accept(newValue);
        }
    }

    private AgencyResponseDTO convertToDTO(Agency agency) {
        return AgencyResponseDTO.builder()
                .id(agency.getId())
                .name(agency.getName())
                .photoUrl(agency.getPhotoUrl())
                .address(agency.getAddress())
                .phone(agency.getPhone())
                .city(CityBasicDTO.builder().name(agency.getCity().getName()).build())
                .email(agency.getUser().getEmail())
                .build();
    }
}