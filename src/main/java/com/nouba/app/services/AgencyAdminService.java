package com.nouba.app.services;

import com.nouba.app.dto.*;
import com.nouba.app.entities.Agency;
import com.nouba.app.entities.City;
import com.nouba.app.entities.User;
import com.nouba.app.entities.Role;
import com.nouba.app.exceptions.auth.UserAlreadyExistsException;
import com.nouba.app.repositories.AgencyRepository;
import com.nouba.app.repositories.CityRepository;
import com.nouba.app.repositories.TicketRepository;
import com.nouba.app.repositories.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgencyAdminService {
    // Références aux repositories nécessaires
    private final AgencyRepository agencyRepository;
    private final CityRepository cityRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TicketRepository ticketRepository;
    private final EmailService emailService; // Service d'envoi d'emails

    /**
     * Crée une nouvelle agence avec son utilisateur associé
     * @param dto DTO contenant les informations de création de l'agence
     * @return DTO de réponse avec les informations de l'agence créée
     * @throws UserAlreadyExistsException si un utilisateur avec cet email existe déjà
     */
    @Transactional
    public AgencyResponseDTO createAgency(AgencyCreateDTO dto) {
        // Vérifie si l'email existe déjà
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("Un utilisateur avec cet email existe déjà.");
        }

        // Récupère la ville correspondante
        City city = cityRepository.findById(dto.getCityId())
                .orElseThrow(() -> new RuntimeException("Ville non trouvée"));

        // Création et sauvegarde de l'utilisateur associé
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.AGENCY);
        user.setEnabled(true);
        user = userRepository.save(user);

        // Création et sauvegarde de l'agence
        Agency agency = new Agency();
        agency.setName(dto.getName());
        agency.setAddress(dto.getAddress());
        agency.setPhone(dto.getPhone());
        //agency.setPhotoUrl(dto.getPhotoUrl()); // Définit l'URL de la photo
        agency.setCity(city);
        agency.setUser(user);
        agency = agencyRepository.save(agency);

        // Envoi d'un email de confirmation à l'agence
        emailService.sendAgencyCreationEmail(
                dto.getEmail(),
                dto.getName(),
                dto.getPhone(),
                dto.getAddress(),
                city.getName(),
                dto.getPassword()
        );

        return mapToResponseDTO(agency);
    }

    /**
     * Met à jour une agence existante (mise à jour partielle)
     * @param agencyId ID de l'agence à mettre à jour
     * @param updateDTO DTO contenant les nouvelles valeurs
     * @param cityId ID de la ville (requis)
     * @return DTO de réponse avec l'agence mise à jour
     */
    @Transactional
    public AgencyResponseDTO updateAgency(Long agencyId, AgencyUpdateDTO updateDTO, @NotNull Long cityId) {
        // Récupère l'agence existante
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new RuntimeException("Agence non trouvée avec l'ID: " + agencyId));

        // Met à jour la ville si nécessaire
        if (updateDTO.getCityId() != null && !updateDTO.getCityId().equals(agency.getCity().getId())) {
            City newCity = cityRepository.findById(updateDTO.getCityId())
                    .orElseThrow(() -> new RuntimeException("Ville non trouvée avec l'ID: " + updateDTO.getCityId()));
            agency.setCity(newCity);
        }

        // Met à jour les champs non-nuls
        updateIfNotNull(updateDTO.getName(), agency::setName);
        updateIfNotNull(updateDTO.getAddress(), agency::setAddress);
        updateIfNotNull(updateDTO.getPhone(), agency::setPhone);
        //updateIfNotNull(updateDTO.getPhotoUrl(), agency::setPhotoUrl); // Ajout pour la photo

        // Met à jour l'utilisateur associé
        User user = agency.getUser();
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().equals(user.getEmail())) {
            user.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getName() != null && !updateDTO.getName().equals(user.getName())) {
            user.setName(updateDTO.getName());
        }
        userRepository.save(user);

        // Sauvegarde finale
        Agency updatedAgency = agencyRepository.save(agency);
        return mapToResponseDTO(updatedAgency);
    }

    /**
     * Méthode utilitaire pour les mises à jour conditionnelles
     * @param newValue Nouvelle valeur à définir
     * @param setter Méthode setter à appeler
     * @param <T> Type de la valeur
     */
    private <T> void updateIfNotNull(T newValue, Consumer<T> setter) {
        if (newValue != null) {
            setter.accept(newValue);
        }
    }

    /**
     * Supprime une agence et son utilisateur associé
     * @param id ID de l'agence à supprimer
     * @param userId ID de l'utilisateur demandant la suppression (optionnel)
     */
    @Transactional
    public void deleteAgency(Long id, Long userId) {
        // Vérifie l'existence de l'agence
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agence non trouvée avec l'ID: " + id));

        // Vérifie les permissions si userId est fourni
        if (userId != null) {
            User requestingUser = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Utilisateur demandeur non trouvé"));

            if (!hasDeletePermission(requestingUser, agency)) {
                throw new RuntimeException("Permission refusée pour la suppression");
            }
        }

        // Récupère l'utilisateur associé
        User associatedUser = agency.getUser();

        // Supprime d'abord l'agence
        agencyRepository.delete(agency);

        // Puis supprime l'utilisateur associé
        if (associatedUser != null) {
            userRepository.delete(associatedUser);
        }
    }

    /**
     * Vérifie les permissions de suppression
     * @param user Utilisateur demandant la suppression
     * @param agency Agence à supprimer
     * @return true si l'utilisateur a les droits
     */
    private boolean hasDeletePermission(User user, Agency agency) {
        // ADMIN peut tout supprimer
        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        // Un utilisateur peut supprimer sa propre agence
        User agencyUser = agency.getUser();
        return agencyUser != null && user.getId().equals(agencyUser.getId());
    }

    /**
     * Récupère toutes les agences
     * @return Liste des DTO des agences
     */
    public List<AgencyResponseDTO> getAllAgencies() {
        return agencyRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère une agence par son ID
     * @param id ID de l'agence
     * @return DTO de l'agence
     */
    public AgencyResponseDTO getAgencyById(Long id) {
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agency not found"));
        return mapToResponseDTO(agency);
    }

    /**
     * Récupère les agences d'une ville
     * @param cityId ID de la ville
     * @return Liste des DTO des agences
     */
    public List<AgencyResponseDTO> getAgenciesByCityId(Long cityId) {
        return agencyRepository.findByCityId(cityId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convertit une entité Agency en DTO de réponse
     * @param agency Entité Agency
     * @return DTO de réponse
     */
    private AgencyResponseDTO mapToResponseDTO(Agency agency) {
        return AgencyResponseDTO.builder()
                .id(agency.getId())
                .name(agency.getName())
                .address(agency.getAddress())
                .phone(agency.getPhone())
                //.photoUrl(agency.getPhotoUrl()) // Inclut l'URL de la photo
                .cityName(agency.getCity().getName())
                .email(agency.getUser().getEmail())
                .build();
    }
}