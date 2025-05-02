package com.nouba.app.controller;

import com.nouba.app.dto.*;
import com.nouba.app.exceptions.auth.UserAlreadyExistsException;
import com.nouba.app.services.AgencyAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur pour la gestion administrative des agences
 */
@RestController
@RequestMapping("/admin/agencies")
@RequiredArgsConstructor
public class AgencyAdminController {

    private final AgencyAdminService agencyAdminService;

    /**
     * Endpoint pour uploader une photo d'agence
     * @param file Le fichier image à uploader
     * @return ResponseEntity contenant l'URL de la photo uploadée

    @PostMapping("/upload-photo")
    public ResponseEntity<ApiResponse<String>> uploadPhoto(
            @RequestParam("file") MultipartFile file) {
        try {
            String photoUrl = fileStorageService.storeFile(file);
            return ResponseEntity.ok(
                    new ApiResponse<>(
                            photoUrl,
                            "Photo téléversée avec succès",
                            HttpStatus.OK.value()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(
                            null,
                            "Erreur lors du téléversement: " + e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }*/

    /**
     * Crée une nouvelle agence avec possibilité d'upload de photo
     * @param agencyDTO DTO de création d'agence contenant les infos de l'agence
     * @return Réponse avec l'agence créée
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AgencyResponseDTO>> addAgency(
            @Valid @RequestBody AgencyCreateDTO agencyDTO) {
        try {
            AgencyResponseDTO response = agencyAdminService.createAgency(agencyDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(
                            response,
                            "Agence créée avec succès",
                            HttpStatus.CREATED.value()
                    ));
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(
                            null,
                            "Un utilisateur avec cet email existe déjà",
                            HttpStatus.CONFLICT.value()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(
                            null,
                            "Erreur interne lors de la création de l'agence",
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    /**
     * Met à jour une agence existante
     * @param id ID de l'agence
     * @param updateDTO DTO de mise à jour
     * @return Réponse avec l'agence mise à jour
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AgencyResponseDTO>> updateAgency(
            @PathVariable Long id,
            @RequestBody AgencyUpdateDTO updateDTO) {
        AgencyResponseDTO response = agencyAdminService.updateAgency(id, updateDTO, updateDTO.getCityId());
        return ResponseEntity.ok(
                new ApiResponse<>(
                        response,
                        "Agence mise à jour avec succès",
                        HttpStatus.OK.value()
                )
        );
    }

    /**
     * Supprime une agence après vérification des droits de l'utilisateur
     * @param id ID de l'agence à supprimer
     * @param userId ID de l'utilisateur effectuant la suppression (optionnel)
     * @return Confirmation de suppression
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAgency(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {  // Rend le paramètre optionnel
        try {
            agencyAdminService.deleteAgency(id, userId);
            return ResponseEntity.ok(
                    new ApiResponse<>(
                            null,
                            "Agence supprimée avec succès",
                            HttpStatus.OK.value()
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(
                            null,
                            "Erreur lors de la suppression: " + e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }
}