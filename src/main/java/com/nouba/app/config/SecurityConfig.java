package com.nouba.app.config;

import com.nouba.app.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableMethodSecurity
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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/admin/agencies").permitAll()
                        .requestMatchers("/public/tickets/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/tickets/agency/*/services").permitAll()

                        // Admin endpoints
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/tickets/admin/**").hasRole("ADMIN")
                        .requestMatchers("/users/**").hasRole("ADMIN")

                        // In the authorizeHttpRequests section of SecurityConfig.java
                        .requestMatchers(HttpMethod.GET, "/tickets/agency/*/services-list").hasRole("AGENCY")

                        // Ticket creation endpoint (keep original URL)
                        .requestMatchers(HttpMethod.POST, "/tickets/agency/*/*/*").hasRole("CLIENT")

                        // new api
                        // In the authorizeHttpRequests section:
                        .requestMatchers(HttpMethod.GET, "/tickets/agency/*/next-number").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.POST, "/tickets/agency/*/*/*/NOUBA*").hasRole("CLIENT")
                        //finish

                        // In the authorizeHttpRequests section of SecurityConfig.java
                        .requestMatchers(HttpMethod.PUT, "/tickets/agency/*/start-first-pending").hasRole("AGENCY")

                        // In the authorizeHttpRequests section:
                        .requestMatchers(HttpMethod.GET, "/tickets/agency/*/last-pending").hasRole("CLIENT")

                        // Client endpoints
                        .requestMatchers(HttpMethod.GET, "/tickets/*/status").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.GET, "/tickets/*/ahead").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.PUT, "/tickets/*/cancel").hasAnyRole("CLIENT", "AGENCY")
                        //.requestMatchers(HttpMethod.PUT, "/tickets/*/cancel-pending").hasAnyRole("CLIENT")
                        .requestMatchers(HttpMethod.PUT, "/tickets/*/cancel-pending").hasAnyRole("AGENCY")//


                        // Agency endpoints
                        .requestMatchers("/tickets/agency/**").hasRole("AGENCY")
                        .requestMatchers(HttpMethod.PUT, "/tickets/agency/*/serve").hasRole("AGENCY")
                        .requestMatchers(HttpMethod.GET, "/tickets/agency/*/current").hasRole("AGENCY")
                        .requestMatchers(HttpMethod.GET, "/tickets/agency/*/pending").hasRole("AGENCY")
                        .requestMatchers(HttpMethod.GET, "/tickets/agency/*/pending/count").hasRole("AGENCY")
                        .requestMatchers(HttpMethod.GET, "/tickets/agency/*/history").hasAnyRole("AGENCY", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/tickets/agency/*/today/**").hasAnyRole("AGENCY", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/tickets/agency/*/all").hasRole("AGENCY")
                        .requestMatchers(HttpMethod.GET, "/tickets/agency/*/clients").hasRole("AGENCY")

                        // Ticket processing endpoints
                        .requestMatchers(HttpMethod.PUT, "/tickets/*/start-service").hasRole("AGENCY")
                        .requestMatchers(HttpMethod.PUT, "/tickets/*/complete-service").hasRole("AGENCY")
                        .requestMatchers(HttpMethod.PUT, "/tickets/*/cancel-active").hasRole("AGENCY")

                        // Agency stats
                        .requestMatchers("/agencies/**").hasAnyRole("ADMIN", "AGENCY", "CLIENT")



                        // All other requests require authentication
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
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}