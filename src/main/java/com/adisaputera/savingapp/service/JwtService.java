package com.adisaputera.savingapp.service;

import com.adisaputera.savingapp.model.RoleEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class JwtService {

    @Value("${app.security.jwt.secret}")
    private String jwtSecret;

    @Value("${app.security.jwt.issuer}")
    private String jwtIssuer;

    @Value("${app.security.jwt.access-token-ttl-sec}")
    private Long accessTokenTtl;

    @Value("${app.security.jwt.refresh-token-ttl-sec}")
    private Long refreshTokenTtl;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generate access token with user information
     */
    public String generateAccessToken(String userId, String fullName, RoleEnum role) {
        Instant now = Instant.now();
        Instant expiration = now.plus(accessTokenTtl, ChronoUnit.SECONDS);

        return Jwts.builder()
                .setIssuer(jwtIssuer)
                .setSubject(userId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .claim("fullName", fullName)
                .claim("role", role.name())
                .claim("type", "access")
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generate refresh token
     */
    public String generateRefreshToken(String userId, String fullName, RoleEnum role) {
        Instant now = Instant.now();
        Instant expiration = now.plus(refreshTokenTtl, ChronoUnit.SECONDS);

        return Jwts.builder()
                .setIssuer(jwtIssuer)
                .setSubject(userId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .setId(UUID.randomUUID().toString())
                .claim("fullName", fullName)
                .claim("role", role.name())
                .claim("type", "refresh")
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Parse and validate JWT token
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .requireIssuer(jwtIssuer)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            throw new RuntimeException("Token has expired");
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported: {}", e.getMessage());
            throw new RuntimeException("Token is not supported");
        } catch (MalformedJwtException e) {
            log.warn("JWT token is malformed: {}", e.getMessage());
            throw new RuntimeException("Token is malformed");
        } catch (SecurityException e) {
            log.warn("JWT signature validation failed: {}", e.getMessage());
            throw new RuntimeException("Token signature is invalid");
        } catch (IllegalArgumentException e) {
            log.warn("JWT token is null, empty or only whitespace: {}", e.getMessage());
            throw new RuntimeException("Token is null or empty");
        }
    }

    /**
     * Extract user ID from token
     */
    public String extractUserId(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * Extract full name from token
     */
    public String extractFullName(String token) {
        Claims claims = parseToken(token);
        return claims.get("fullName", String.class);
    }

    /**
     * Extract role from token
     */
    public String extractRole(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    /**
     * Extract token type (access/refresh)
     */
    public String extractTokenType(String token) {
        Claims claims = parseToken(token);
        return claims.get("type", String.class);
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate token and check if it's an access token
     */
    public boolean isValidAccessToken(String token) {
        try {
            Claims claims = parseToken(token);
            String tokenType = claims.get("type", String.class);
            return "access".equals(tokenType) && isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate token and check if it's a refresh token
     */
    public boolean isValidRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            String tokenType = claims.get("type", String.class);
            return !"refresh".equals(tokenType) || !isTokenExpired(token);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Get token expiration time in seconds
     */
    public Long getTokenExpirationTime(String tokenType) {
        return "refresh".equals(tokenType) ? refreshTokenTtl : accessTokenTtl;
    }

    /**
     * Extract all user information from token
     */
    public JwtUserInfo extractUserInfo(String token) {
        Claims claims = parseToken(token);
        return JwtUserInfo.builder()
                .userId(claims.getSubject())
                .fullName(claims.get("fullName", String.class))
                .role(claims.get("role", String.class))
                .tokenType(claims.get("type", String.class))
                .issuedAt(claims.getIssuedAt())
                .expiration(claims.getExpiration())
                .build();
    }

    /**
     * Extract JTI (unique identifier) from token
     */
    public String extractJti(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getId();
        } catch (Exception e) {
            log.warn("Failed to extract JTI from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Inner class to hold user information extracted from JWT
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JwtUserInfo {
        private String userId;
        private String fullName;
        private String role;
        private String tokenType;
        private Date issuedAt;
        private Date expiration;
    }
}