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

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;


    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {

        String token = authService.login(loginRequest);

        return ResponseEntity.ok(new ApiResponse(token, "Success", 200)); // 200 est le code HTTP de succès.
    }

    @PostMapping("/register-client")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody ClientRegisterRequest request) throws Exception {

        ApiResponse<String> response = authService.register(request);

        return ResponseEntity.ok(response);  // Code HTTP 200, avec la réponse d'inscription.
    }

    @GetMapping("/activate")
    public ResponseEntity<ApiResponse<String>> activateAccount(@RequestParam String token) {
        ApiResponse<String> response = authService.activateAccount(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            authService.forgotPassword(request.getEmail());
        } catch (Exception e) {
            throw new RuntimeException(e); // Exception pour interrompre l'exécution.
        }

        return ResponseEntity.ok(new ApiResponse<>("Email de réinitialisation envoyé !", HttpStatus.OK.value()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(new ApiResponse<>("Mot de passe réinitialisé avec succès !", HttpStatus.OK.value()));
    }
}