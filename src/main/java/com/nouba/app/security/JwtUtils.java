package com.nouba.app.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.stream.Collectors;
import java.util.List;
import javax.crypto.SecretKey;

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

        return Jwts.builder()
                .setSubject(username) // Set email or username
                .claim("roles", roles) // Embed roles inside the token
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
}
