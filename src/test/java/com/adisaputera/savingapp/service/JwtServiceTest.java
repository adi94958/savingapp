package com.adisaputera.savingapp.service;

import com.adisaputera.savingapp.model.RoleEnum;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "app.security.jwt.issuer=savings-api",
    "app.security.jwt.secret=N6THefB3IXZyVvpjkAJpch8P7LKK8T1aTVkGN4ClPTA=",
    "app.security.jwt.access-token-ttl-sec=3600",
    "app.security.jwt.refresh-token-ttl-sec=86400"
})
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    void testJwtConfigurationIsUsed() {
        // Given
        String userId = "test-user-id";
        String fullName = "Test User";
        RoleEnum role = RoleEnum.nasabah;

        // When
        String accessToken = jwtService.generateAccessToken(userId, fullName, role);
        String refreshToken = jwtService.generateRefreshToken(userId, fullName, role);

        // Then
        assertNotNull(accessToken);
        assertNotNull(refreshToken);

        // Verify access token contains correct issuer
        Claims accessClaims = jwtService.parseToken(accessToken);
        assertEquals("savings-api", accessClaims.getIssuer());
        assertEquals(userId, accessClaims.getSubject());
        assertEquals(fullName, accessClaims.get("fullName", String.class));
        assertEquals("nasabah", accessClaims.get("role", String.class));
        assertEquals("access", accessClaims.get("type", String.class));

        // Verify refresh token contains correct issuer
        Claims refreshClaims = jwtService.parseToken(refreshToken);
        assertEquals("savings-api", refreshClaims.getIssuer());
        assertEquals(userId, refreshClaims.getSubject());
        assertEquals(fullName, refreshClaims.get("fullName", String.class));
        assertEquals("nasabah", refreshClaims.get("role", String.class));
        assertEquals("refresh", refreshClaims.get("type", String.class));

        // Verify token TTL configuration
        assertEquals(3600L, jwtService.getTokenExpirationTime("access"));
        assertEquals(86400L, jwtService.getTokenExpirationTime("refresh"));

        // Verify tokens are valid
        assertTrue(jwtService.isValidAccessToken(accessToken));
        assertTrue(jwtService.isValidRefreshToken(refreshToken));

        // Verify user info extraction
        JwtService.JwtUserInfo userInfo = jwtService.extractUserInfo(accessToken);
        assertEquals(userId, userInfo.getUserId());
        assertEquals(fullName, userInfo.getFullName());
        assertEquals("nasabah", userInfo.getRole());
        assertEquals("access", userInfo.getTokenType());
    }
}