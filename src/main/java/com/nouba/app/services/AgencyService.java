package com.nouba.app.services;

import com.nouba.app.dto.*;
import com.nouba.app.entities.*;
import com.nouba.app.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgencyService {
    private final AgencyRepository agencyRepository;
    private final TicketRepository ticketRepository;

    public List<AgencyResponseDTO> getAllAgencies() {
        return agencyRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AgencyResponseDTO> getAgenciesByCity(Long cityId) {
        return agencyRepository.findByCityId(cityId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public AgencyResponseDTO getAgencyById(Long id) {
        return agencyRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Agency not found"));
    }

    /**public int getQueueCount(Long agencyId) {
        return ticketRepository.countByAgencyIdAndServedFalse(agencyId);
    }

    public String getCurrentNumber(Long agencyId) {
        return ticketRepository.findFirstByAgencyIdAndServedFalseOrderByNumberAsc(agencyId)
                .map(Ticket::getNumber)
                .orElse(null);
    }*/

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