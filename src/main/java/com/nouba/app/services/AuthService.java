package com.nouba.app.services;



import com.nouba.app.dto.*;
import com.nouba.app.entities.Agency;
import com.nouba.app.entities.Client;
import com.nouba.app.entities.Role;
import com.nouba.app.entities.User;
import com.nouba.app.entities.City;
import com.nouba.app.exceptions.auth.*;
import com.nouba.app.exceptions.email.SendingEmailException;
import com.nouba.app.repositories.AgencyRepository;
import com.nouba.app.repositories.CityRepository;
import com.nouba.app.repositories.ClientRepository;
import com.nouba.app.repositories.UserRepository;
import com.nouba.app.security.JwtUtils;
import jakarta.mail.MessagingException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {
    // Proper logger initialization
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AgencyRepository agencyRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    // register admin
    // Dans AuthService.java
    @Transactional
    public ApiResponse<String> registerAdmin(AdminRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email déjà utilisé");
        }

        User admin = new User();
        admin.setName(request.getName());
        admin.setEmail(request.getEmail());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true); // Admin activé directement
        admin.setActivationToken(UUID.randomUUID().toString());

        userRepository.save(admin);

        return new ApiResponse<>("Admin créé avec succès", HttpStatus.OK.value());
    }
    // end
   //register_Client
    @Transactional
    public ApiResponse<String> registerClient(ClientRegisterRequest request) throws Exception {
        // Check if email already exists in database
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Un utilisateur avec cet email existe déjà.");
        }

        // Create new User entity from registration request
        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Encrypt password
        user.setEnabled(false); // Account disabled until activation
        user.setActivationToken(UUID.randomUUID().toString()); // Generate unique activation token
        user.setRole(Role.CLIENT); // Set user role to CLIENT
        User savedUser = userRepository.save(user); // Save user to database

        // Create Client profile linked to the User
        Client client = new Client();
        client.setPhone(request.getPhone());
        client.setAddress(request.getAddress());
        client.setUser(savedUser); // Establish relationship
        clientRepository.save(client); // Save client to database

        // Send activation email with activation link
        String activationLink = "http://localhost:4200/auth/activate-account?token=" + user.getActivationToken();
        Map<String, String> activationVariables = Map.of("activationLink", activationLink);
        String activationContent = emailService.loadEmailTemplate("templates/emails/activation-email.html", activationVariables);

        // Envoyer l'email
        try {
            emailService.sendEmail(user.getEmail(), "Activation de votre compte", activationContent);
        } catch (MessagingException e) {
            throw new SendingEmailException("Erreur lors de l'envoi de l'email d'activation.");
        }


        // Send welcome email with account details
        Map<String, String> welcomeVariables = Map.of(
                "name", user.getName(),
                "email", user.getEmail(),
                "password", request.getPassword(), // Sending plain password (as requested)
                "phone", client.getPhone(),
                "address", client.getAddress()
        );

        String welcomeContent = emailService.loadEmailTemplate("templates/emails/welcome-client.html", welcomeVariables);
        emailService.sendEmail(user.getEmail(), "Bienvenue chez Nouba", welcomeContent);

        return new ApiResponse<>("User registered successfully! Please check your email to activate your account.", HttpStatus.OK.value());

    }

   
    // --------------------------------------------------
    @Transactional
    public ApiResponse<String> registerAgency(AgencyRegisterRequest request) throws Exception {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Un utilisateur avec cet email existe déjà.");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.setActivationToken(UUID.randomUUID().toString());
        user.setRole(Role.AGENCY);
        User SavedUser = userRepository.save(user);

        Agency agency = new Agency();
        agency.setPhone(request.getPhone());
        agency.setAddress(request.getAddress());
        agency.setUser(SavedUser);
        City city = cityRepository.findById(request.getCityId()).orElseThrow();
        agency.setCity(city);
        agencyRepository.save(agency);

        String activationLink = "http://localhost:4200/auth/activate-account?token=" + user.getActivationToken();

        //Charger et personnaliser le modèle d'email
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
    //login
    public String login(LoginRequest loginRequest) throws Exception {
        // Authenticate user with Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        if (authentication.isAuthenticated()) {
            Optional<User> user = userRepository.findByEmail(loginRequest.getEmail());

            // Check if account is activated
            if (!user.get().isEnabled()) {
                throw new AccountIsNotEnabledException("Votre compte n'est pas activé. Veuillez vérifier votre email pour le lien d'activation.");
            }



            // Send login notification email
            Map<String, String> loginVariables = Map.of(
                    "name", user.get().getName(),
                    "loginTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );
            String loginContent = emailService.loadEmailTemplate("templates/emails/login-notification.html", loginVariables);
            emailService.sendEmail(user.get().getEmail(), "Connexion réussie", loginContent);

            // Create a new authentication with user details
            UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                    authentication.getPrincipal(),
                    authentication.getCredentials(),
                    authentication.getAuthorities()
            );

            // Add user details to authentication
            Map<String, Object> details = Map.of(
                    "id", user.get().getId(),  // Add this line
                    "name", user.get().getName(),
                    "email", user.get().getEmail(),
                    "role", user.get().getRole().name()
            );
            newAuth.setDetails(details);
            // Generate JWT token for authenticated user
            return jwtUtils.generateToken(newAuth);
        } else {
            throw new InvalidCredentialsException("Email ou mot de passe invalide.");
        }
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
        String resetLink = "http://localhost:4200/auth/reset-password?token=" + resetToken;

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

   /** public ApiResponse<String>  activateAccount(String token) {
        Optional<User> userOptional = userRepository.findByActivationToken(token);

        if (userOptional.isEmpty()) {
            throw new InvalidTokenException("Token invalide !");
        }

        User user = userOptional.get();
        user.setEnabled(true);
        user.setActivationToken(null);
        userRepository.save(user);

        return new ApiResponse<>("User registered successfully! Please check your email to activate your account.", HttpStatus.OK.value());
    }*/

    /**
     * Activates a user account using the provided activation token and sends a confirmation email.
     *
     * @param token The unique activation token sent to the user's email
     * @return ApiResponse with activation success message
     * @throws InvalidTokenException if the token is invalid or expired
     * */

    public ApiResponse<String>  activateAccount(String token) {
        Optional<User> userOptional = userRepository.findByActivationToken(token);

        if (userOptional.isEmpty()) {
            throw new InvalidTokenException("Token invalide !");
        }

        User user = userOptional.get();
        user.setEnabled(true);
        user.setActivationToken(null);
        userRepository.save(user);

        return new ApiResponse<>("User registered successfully! Please check your email to activate your account.", HttpStatus.OK.value());
    }
  
}