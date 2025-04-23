package com.nouba.app.controller;

import com.nouba.app.dto.*;
import com.nouba.app.repositories.CityRepository;
import com.nouba.app.services.AuthService;
import com.nouba.app.services.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController // Cette annotation permet à Spring de reconnaître cette classe comme un contrôleur REST. Elle traite les requêtes HTTP et renvoie des réponses au format JSON ou d'autres formats.
@RequestMapping("/auth") // Définit la route de base pour toutes les méthodes de ce contrôleur. Toutes les requêtes commenceront par "/auth", suivies de la méthode spécifique.
public class AuthController {

    // Injection des dépendances via le constructeur.
    // Chaque service est injecté par Spring pour éviter de les créer manuellement, ce qui favorise l'inversion de contrôle (IoC).

    private final AuthService authService; // Service qui gère l'authentification (login, registration, activation, etc.)
    private final CityRepository cityRepository; // Référentiel pour interagir avec la base de données des utilisateurs.
    private final EmailService emailService; // Service pour l'envoi d'e-mails, utilisé pour envoyer des liens d'activation et de réinitialisation de mot de passe.
    private final PasswordEncoder passwordEncoder; // Encodage du mot de passe pour le stockage sécurisé des mots de passe des utilisateurs.

    // Constructeur avec injection des dépendances.
    public AuthController(AuthService authService, CityRepository cityRepository, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.authService = authService; // Initialisation de l'authentification service.
        this.cityRepository = cityRepository; // Initialisation du repository utilisateur.
        this.emailService = emailService; // Initialisation du service d'email.
        this.passwordEncoder = passwordEncoder; // Initialisation du service d'encodage de mot de passe.
    }

    // Méthode pour gérer la connexion de l'utilisateur (login).
    @PostMapping("/login") // Cette annotation définit une route HTTP POST pour la connexion.
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        // La méthode prend un `LoginRequest` qui contient les informations nécessaires pour se connecter (email et mot de passe).
        // @Valid s'assure que les données de la requête sont validées avant de procéder à l'exécution.

        // Appel du service d'authentification pour vérifier les informations de connexion et générer un token JWT.
        String token = authService.login(loginRequest);

        // Retourner une réponse avec le token d'authentification et un message de succès.
        return ResponseEntity.ok(new ApiResponse(token, "Success", 200)); // 200 est le code HTTP de succès.
    }

    // Méthode pour inscrire un nouvel utilisateur.
    @PostMapping("/register-client") // Cette annotation définit une route HTTP POST pour l'enregistrement d'un nouvel utilisateur.
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody ClientRegisterRequest request) throws Exception {
        // La méthode prend un `RegisterRequest` qui contient les informations nécessaires pour inscrire un nouvel utilisateur.
        // @Valid s'assure que les données de la requête sont validées avant de procéder à l'enregistrement.

        // Appel du service d'authentification pour inscrire l'utilisateur en base de données.
        ApiResponse<String> response = authService.registerClient(request);

        // Retourner une réponse JSON avec un message de succès d'enregistrement.
        return ResponseEntity.ok(response);  // Code HTTP 200, avec la réponse d'inscription.
    }
    @PostMapping("/register-agency") // Cette annotation définit une route HTTP POST pour l'enregistrement d'un nouvel utilisateur.
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody AgencyRegisterRequest request) throws Exception {
        // La méthode prend un `RegisterRequest` qui contient les informations nécessaires pour inscrire un nouvel utilisateur.
        // @Valid s'assure que les données de la requête sont validées avant de procéder à l'enregistrement.

        // Appel du service d'authentification pour inscrire l'utilisateur en base de données.
        ApiResponse<String> response = authService.registerAgency(request);

        // Retourner une réponse JSON avec un message de succès d'enregistrement.
        return ResponseEntity.ok(response);  // Code HTTP 200, avec la réponse d'inscription.
    }
    //add register-admin
    // Dans AuthController.java
    @PostMapping("/register-admin")
    public ResponseEntity<ApiResponse<String>> registerAdmin(
            @Valid @RequestBody AdminRegisterRequest request) {
        ApiResponse<String> response = authService.registerAdmin(request);
        return ResponseEntity.ok(response);
    }


    // Méthode pour activer un compte utilisateur via un token envoyé par e-mail.
    @GetMapping("/activate") // Cette annotation définit une route HTTP GET pour activer le compte de l'utilisateur via un token.
    public ResponseEntity<ApiResponse<String>> activateAccount(@RequestParam String token) {
        // Le token est passé en tant que paramètre de requête pour activer le compte de l'utilisateur.

        // Appel du service pour activer le compte avec le token.
        ApiResponse<String> response = authService.activateAccount(token);

        // Retourner une réponse JSON avec un message de succès d'activation de compte.
        return ResponseEntity.ok(response);  // Code HTTP 200 avec message de succès.
    }

    // Méthode pour envoyer un e-mail de réinitialisation du mot de passe.
    @PostMapping("/forgot-password") // Cette annotation définit une route HTTP POST pour la demande de réinitialisation de mot de passe.
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            // Appel du service pour envoyer un e-mail avec un lien de réinitialisation de mot de passe.
            authService.forgotPassword(request.getEmail());
        } catch (Exception e) {
            // Si une erreur survient lors de l'envoi de l'email, une exception est levée.
            throw new RuntimeException(e); // Exception pour interrompre l'exécution.
        }

        // Retourner une réponse JSON avec un message confirmant que l'email a été envoyé pour la réinitialisation.
        return ResponseEntity.ok(new ApiResponse<>("Email de réinitialisation envoyé !", HttpStatus.OK.value()));
    }

    // Méthode pour réinitialiser le mot de passe de l'utilisateur via un token et un nouveau mot de passe.
    @PostMapping("/reset-password") // Cette annotation définit une route HTTP POST pour réinitialiser le mot de passe.
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        // Le token et le nouveau mot de passe sont passés en tant que paramètres de requête.

        // Appel du service pour réinitialiser le mot de passe de l'utilisateur avec le token et le nouveau mot de passe.
        authService.resetPassword(request.getToken(), request.getNewPassword());

        // Retourner une réponse JSON avec un message de succès indiquant que le mot de passe a été réinitialisé avec succès.
        return ResponseEntity.ok(new ApiResponse<>("Mot de passe réinitialisé avec succès !", HttpStatus.OK.value()));
    }
}