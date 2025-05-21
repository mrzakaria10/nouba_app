package com.nouba.app.config;

import com.nouba.app.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors ->{})
                .authorizeHttpRequests(auth -> auth
                        // Permettre l'accès sans authentification
                        .requestMatchers("/auth/**").permitAll()

                        // Endpoints admin - réservés aux ADMIN
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")

                        // Configuration des endpoints API avec restrictions de rôle
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/admin/agencies").permitAll()

                        // Explicitly secure ticket endpoints
                        .requestMatchers("/tickets/**").authenticated()

                        // Add to requestMatchers in SecurityConfig.java
                        .requestMatchers("/users/active-this-week").hasRole("ADMIN")

                        //public
                        .requestMatchers("/public/tickets/**").permitAll()

                        .requestMatchers("/tickets/admin/**").hasRole("ADMIN")

                        .requestMatchers("/tickets/**").hasAnyRole("AGENCY")

                        .requestMatchers("/users").hasRole("ADMIN")
                        .requestMatchers("/users/role/**").hasAnyRole("ADMIN", "AGENCY", "CLIENT")
                        .requestMatchers("/users/**").hasRole("ADMIN")
                        .requestMatchers("/agencies/*/stats").hasAnyRole("ADMIN", "AGENCY")


                        // Toutes les autres requêtes nécessitent une authentification
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}