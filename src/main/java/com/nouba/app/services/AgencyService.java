package com.nouba.app.services;

import com.nouba.app.dto.AgencyResponseDTO;
import com.nouba.app.entities.Agency;
import com.nouba.app.entities.Ticket;
import com.nouba.app.repositories.AgencyRepository;
import com.nouba.app.repositories.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgencyService {

    private final AgencyRepository agencyRepository;
    private final TicketRepository ticketRepository;

    /**
     * Récupère toutes les agences avec leurs informations
     * @return Liste des DTO d'agences
     */
    public List<AgencyResponseDTO> getAllAgencies() {
        // Charge explicitement les relations nécessaires
        List<Agency> agencies = agencyRepository.findAllWithRelations();
        return agencies.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les agences d'une ville spécifique
     * @param cityId ID de la ville
     * @return Liste des DTO d'agences de la ville
     */
    public List<AgencyResponseDTO> getAgenciesByCity(Long cityId) {
        return agencyRepository.findByCityId(cityId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convertit une entité Agency en DTO
     * @param agency Entité à convertir
     * @return DTO converti
     * @throws IllegalStateException si conversion impossible
     */
    private AgencyResponseDTO convertToDTO(Agency agency) {
        if (agency == null) {
            throw new IllegalStateException("L'agence ne peut pas être null");
        }

        return AgencyResponseDTO.builder()
                .id(agency.getId())
                .name(agency.getName())
                .address(agency.getAddress())
                .phone(agency.getPhone())
                .cityName(agency.getCity() != null ? agency.getCity().getName() : "Inconnue")
                .email(agency.getUser() != null ? agency.getUser().getEmail() : "Inconnu")
                .build();
    }

    /**
     * Récupère le nombre de personnes en attente dans une agence
     * @param agencyId ID de l'agence
     * @return Nombre de tickets non servis (personnes en attente)
     */
    public int getQueueCount(Long agencyId) {
        return ticketRepository.countByAgencyIdAndServedFalse(agencyId);
    }

    /**
     * Récupère le numéro actuellement servi dans l'agence
     * @param agencyId ID de l'agence
     * @return Numéro du ticket en cours ou null si aucun ticket en attente
     */
    public Integer getCurrentNumber(Long agencyId) {
        return ticketRepository.findTopByAgencyIdAndServedFalseOrderByNumberAsc(agencyId)
                .map(Ticket::getNumber)
                .orElse(null);
    }
}