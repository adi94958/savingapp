package com.adisaputera.savingapp.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.adisaputera.savingapp.dto.request.LoginRequestDTO;
import com.adisaputera.savingapp.dto.request.RefreshTokenRequestDTO;
import com.adisaputera.savingapp.dto.response.LoginResponseDTO;
import com.adisaputera.savingapp.dto.response.RefreshTokenResponseDTO;
import com.adisaputera.savingapp.dto.response.UserResponseDTO;
import com.adisaputera.savingapp.exception.ResourceNotFoundException;
import com.adisaputera.savingapp.exception.UnauthorizedException;
import com.adisaputera.savingapp.model.User;
import com.adisaputera.savingapp.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenBlacklistService blacklistService;

    public LoginResponseDTO login(LoginRequestDTO request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if(userOptional.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOptional.get().getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        User user = userOptional.get();
        String accessToken = jwtService.generateAccessToken(
            user.getId().toString(), 
            user.getFullName(), 
            user.getRole()
        );

        String refreshToken = jwtService.generateRefreshToken(
            user.getId().toString(), 
            user.getFullName(), 
            user.getRole()
        );
        
        UserResponseDTO userResponse = UserResponseDTO.builder()
        .id(user.getId().toString())
        .fullName(user.getFullName())
        .email(user.getEmail())
        .address(user.getAddress())
        .phone(user.getPhone())
        .createdAt(user.getCreatedAt().toString())
        .build();

        return new LoginResponseDTO(
            accessToken,
            refreshToken,
            "Bearer",
            jwtService.getTokenExpirationTime("access"),
            userResponse
        );
    }

    public RefreshTokenResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        String oldRefreshToken = request.getRefreshToken();

        // Validate token expiration
        if (jwtService.isTokenExpired(oldRefreshToken)) {
            throw new UnauthorizedException("Refresh token has expired");
        }
        
        // Blacklist the old token
        jwtService.parseToken(oldRefreshToken);
        String jti = jwtService.extractJti(oldRefreshToken); 

        if (blacklistService.isBlacklisted(jti)) {
            throw new UnauthorizedException("Refresh token has already been used");
        }

        // Blacklist the old token
        blacklistService.blacklistToken(jti);

        // Extract user info and validate user exists
        JwtService.JwtUserInfo userInfo = jwtService.extractUserInfo(oldRefreshToken);
        Optional<User> userOptional = userRepository.findById(UUID.fromString(userInfo.getUserId()));
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        User user = userOptional.get();
        String newAccessToken = jwtService.generateAccessToken(
            user.getId().toString(),
            user.getFullName(),
            user.getRole()
        );

        return new RefreshTokenResponseDTO(
            newAccessToken,
            "Bearer",
            jwtService.getTokenExpirationTime("access")
        );
    }
}
