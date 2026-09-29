package com.anomaly.transactionanomalybackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    // =========================================================
    // SIGNING KEY
    // =========================================================

    private SecretKey getSigningKey() {

        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException(
                    "JWT secret is not configured."
            );
        }

        byte[] keyBytes =
                secretKey.getBytes(StandardCharsets.UTF_8);

        /*
         * HS256 requires a key of at least 256 bits (32 bytes).
         */
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT secret must contain at least 32 characters."
            );
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    // =========================================================
    // GENERATE TOKEN
    // =========================================================

    public String generateToken(
            String email,
            String role) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email is required to generate a token."
            );
        }

        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException(
                    "Role is required to generate a token."
            );
        }

        if (expirationTime <= 0) {
            throw new IllegalStateException(
                    "JWT expiration must be greater than zero."
            );
        }

        Date issuedAt = new Date();

        Date expiration =
                new Date(
                        issuedAt.getTime() + expirationTime
                );

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    // =========================================================
    // EXTRACT EMAIL
    // =========================================================

    public String extractEmail(String token) {

        return getClaims(token).getSubject();
    }

    // =========================================================
    // EXTRACT ROLE
    // =========================================================

    public String extractRole(String token) {

        return getClaims(token)
                .get("role", String.class);
    }

    // =========================================================
    // VALIDATE TOKEN
    // =========================================================

    public boolean isTokenValid(String token) {

        if (token == null || token.isBlank()) {
            return false;
        }

        try {

            Claims claims = getClaims(token);

            return claims.getSubject() != null
                    && !claims.getSubject().isBlank()
                    && claims.get("role", String.class) != null;

        } catch (Exception exception) {

            return false;
        }
    }

    // =========================================================
    // PARSE JWT CLAIMS
    // =========================================================

    private Claims getClaims(String token) {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT token cannot be empty."
            );
        }

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}