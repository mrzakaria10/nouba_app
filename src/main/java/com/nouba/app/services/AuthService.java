package com.nouba.app.services;



import com.nouba.app.dto.AgencyRegisterRequest;
import com.nouba.app.dto.ApiResponse;
import com.nouba.app.dto.ClientRegisterRequest;
import com.nouba.app.dto.LoginRequest;
import com.nouba.app.entities.Agency;
import com.nouba.app.entities.Client;
import com.nouba.app.entities.Role;
import com.nouba.app.entities.User;
import com.nouba.app.entities.City;
import com.nouba.app.exceptions.auth.*;
import com.nouba.app.exceptions.email.SendingEmailException;
import com.nouba.app.repositories.ClientRepository;
import com.nouba.app.repositories.UserRepository;
import com.nouba.app.security.JwtUtils;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Transactional
    public ApiResponse<String> register(ClientRegisterRequest request) throws Exception {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Un utilisateur avec cet email existe déjà.");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(false);
        user.setActivationToken(UUID.randomUUID().toString());
        user.setRole(Role.CLIENT);
        User SavedUser = userRepository.save(user);

        Client client = new Client();
        client.setPhone(request.getPhone());
        client.setAddress(request.getAddress());
        client.setUser(SavedUser);
        clientRepository.save(client);

        String activationLink = "http://localhost:8080/auth/activate?token=" + user.getActivationToken();

        // Charger et personnaliser le modèle d'email
        Map<String, String> emailVariables = Map.of("activationLink", activationLink);
        String emailContent = emailService.loadEmailTemplate("templates/emails/activation-email.html", emailVariables);

        // Envoyer l'email
        try {
            emailService.sendEmail(user.getEmail(), "Activation de votre compte", emailContent);
        } catch (MessagingException e) {
            throw new SendingEmailException("Erreur lors de l'envoi de l'email d'activation.");
        }

        return new ApiResponse<>("User registered successfully! Please check your email to activate your account.", HttpStatus.OK.value());
    }


    public String login(LoginRequest loginRequest) {
        // Authenticate the user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        // If authentication is successful, generate JWT token
        if (authentication.isAuthenticated()) {
            Optional<User> user = userRepository.findByEmail(loginRequest.getEmail());
            if (!user.get().isEnabled()) {
                throw new AccountIsNotEnabledException("Your account is not activated. Please check your email for the activation link.");
            }
            return jwtUtils.generateToken(authentication);
        } else {
            throw new InvalidCredentialsException("Invalid email or password.");
        }
    }

    public ApiResponse<String>  activateAccount(String token) {
        Optional<User> userOptional = userRepository.findByActivationToken(token);

        if (userOptional.isEmpty()) {
            throw new InvalidTokenException("Token invalide !");
        }

        User user = userOptional.get();
        user.setEnabled(true);
        user.setActivationToken(null);
        userRepository.save(user);

        return new ApiResponse<>("Your account has been activated successfully", HttpStatus.OK.value());
    }

    public void forgotPassword(String email) throws Exception {
        // Check if the user exists with the provided email
        Optional<User> userOptional = userRepository.findByEmail(email);
        System.out.println(userOptional);
        // Throw a custom exception if the email is not found
        if (userOptional.isEmpty()) {
            throw new EmailNotFoundException("Email not found!");
        }

        User user = userOptional.get();
        String resetToken = UUID.randomUUID().toString();
        user.setActivationToken(resetToken);
        userRepository.save(user);

        // Générer le lien de réinitialisation
        String resetLink = "http://localhost:8080/auth/reset-password?token=" + resetToken;

        // Charger et personnaliser le modèle d'email
        Map<String, String> emailVariables = Map.of("resetLink", resetLink);
        String emailContent = emailService.loadEmailTemplate("templates/emails/reset-password-email.html", emailVariables);
        // Send the reset email
        emailService.sendEmail(user.getEmail(), "Réinitialisation du mot de passe", emailContent);
    }

    public void resetPassword(String token, String newPassword) {
        // Check if the user exists with the provided token
        Optional<User> userOptional = userRepository.findByActivationToken(token);

        // If token is invalid, throw a custom exception
        if (userOptional.isEmpty()) {
            throw new InvalidTokenException("Token invalide !");
        }

        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(newPassword));  // Set new password
        user.setActivationToken(null);  // Remove token after use
        userRepository.save(user);
    }

}