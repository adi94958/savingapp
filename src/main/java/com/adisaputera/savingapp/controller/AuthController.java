package com.adisaputera.savingapp.controller;

import com.adisaputera.savingapp.dto.message.ApiResponse;
import com.adisaputera.savingapp.dto.request.LoginRequestDTO;
import com.adisaputera.savingapp.dto.request.RefreshTokenRequestDTO;
import com.adisaputera.savingapp.dto.response.LoginResponseDTO;
import com.adisaputera.savingapp.dto.response.RefreshTokenResponseDTO;
import com.adisaputera.savingapp.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping(
        path = "/login", 
        consumes = "application/json", 
        produces = "application/json"
    )
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO loginResponse = authService.login(request);
        ApiResponse<LoginResponseDTO> response = ApiResponse.success(
                "Login successful", 
                loginResponse
        );
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(
        path = "/refresh", 
        consumes = "application/json", 
        produces = "application/json"
    )
    public ResponseEntity<ApiResponse<RefreshTokenResponseDTO>> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        RefreshTokenResponseDTO refreshTokenResponse = authService.refreshToken(request);
        ApiResponse<RefreshTokenResponseDTO> response = ApiResponse.success(
                "Token refreshed successfully", 
                refreshTokenResponse
        );
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}