package com.nouba.app.security;

import com.nouba.app.entities.Role;
import com.nouba.app.entities.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.Date;
import java.util.stream.Collectors;
import java.util.List;
import javax.crypto.SecretKey;
import java.util.Map;

@Component
public class JwtUtils {

    private final SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode("41751ed65fad56a635261ff79a84bfd9242c65cb311ef009f34d1906afc53654c3c29857d68aa14ea98f5e3ec3bb4ddd792ff432a1d100700ff17a6eca5421efaafd979ba0778d6e4a7c547d0dcc084fbdf0bde89de80c38df04f3e34a5717305fb01ccab9095d4bb932d8310488888167aae9247ada5b67021c5ac4220fd2881b32fb7b2f2eae0df8c25aac72e8da163882738ff80360d73836918b9ef24730ef30f18784022f735498a56127d31e70ffc67c72b623e5aeac7a253061dc0a26dd2b527074b76c672e27f61d8e2781fec39a2723a5b48c47b3ea8562cf7f0f4158e52d4f1a8ccb9d6d325a7924a98f1f324648048db7932296129dc3a35640b6"));
    private final long EXPIRATION_TIME = 86400000; // 1 day

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();

        // Extract roles from the authenticated user
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

// Get the UserDetails implementation
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();



        // Get user details from authentication
        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
        String name = (String) details.get("name");
        String email = (String) details.get("email");
        String role = (String) details.get("role");
        Long id = (Long) details.get("id");

        Long agencyId = null;
        Long clientId = null;

        if (authentication.getPrincipal() instanceof UserDetails) {
            // You'll need to cast to your custom UserDetails implementation if needed
            // and fetch the agency ID from the User entity
            // For example:
            User user = (User) authentication.getPrincipal();
            if (user.getRole() == Role.AGENCY && user.getAgency() != null) {
                agencyId = user.getAgency().getId();
            }
            if (user.getRole() == Role.CLIENT && user.getClient() != null) {
                clientId = user.getClient().getId();
            }

        }


        return Jwts.builder()
                .setSubject(username) // Set email or username
                .claim("roles", roles) // Embed roles inside the token
                .claim("name", name) // Add name to the token
                .claim("email", email) // Add email to the token
                .claim("role", role)
                .claim("id", id)
                .claim("agencyId", agencyId) // Add agency ID to the token
                .claim("clientId", clientId) // Add client ID to the token


                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public List<String> extractRoles(String token) {
        return extractClaims(token).get("roles", List.class);
    }

    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractName(String token) {
        return extractClaims(token).get("name", String.class);
    }

    public String extractEmail(String token) {
        return extractClaims(token).get("email", String.class);
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }
    public Long extractId(String token) {
        return extractClaims(token).get("id", Long.class);
    }
    public Long extractClientId(String token) {
        return extractClaims(token).get("clientId", Long.class);
    }
    public Long extractAgencyId(String token) {
        return extractClaims(token).get("agencyId", Long.class);
    }
}
