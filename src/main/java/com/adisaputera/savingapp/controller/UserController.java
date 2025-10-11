package com.adisaputera.savingapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.adisaputera.savingapp.dto.message.ApiResponse;
import com.adisaputera.savingapp.dto.request.ChangePasswordRequestDTO;
import com.adisaputera.savingapp.dto.request.CreateNasabahRequestDTO;
import com.adisaputera.savingapp.dto.request.UpdateNasabahRequestDTO;
import com.adisaputera.savingapp.dto.response.UserResponseDTO;
import com.adisaputera.savingapp.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping(
        path = "/admin/nasabah/create", 
        produces = "application/json"
    )
    public ResponseEntity<ApiResponse<UserResponseDTO>> createNasabah(@Valid @RequestBody CreateNasabahRequestDTO request) {
        ApiResponse<UserResponseDTO> response = userService.createNasabah(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(
        path = "/admin/nasabah/list", 
        produces = "application/json"
    )
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getNasabahList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int perPage,
            @RequestParam(defaultValue = "fullName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection, 
            @RequestParam(required = false) String keyword) {
        ApiResponse<List<UserResponseDTO>> response = userService.getNasabahList(page, perPage, sortBy, sortDirection, keyword);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(
        path = "/profile/me", 
        produces = "application/json"
    )
    public ResponseEntity<ApiResponse<UserResponseDTO>> getProfile() {
        ApiResponse<UserResponseDTO> response = userService.getProfile();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    @PutMapping(
        path = "/admin/nasabah/{userId}/update", 
        consumes = "application/json", 
        produces = "application/json"
    )
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateNasabah(@PathVariable UUID userId, @RequestBody UpdateNasabahRequestDTO request) {
        ApiResponse<UserResponseDTO> response = userService.updateNasabah(userId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

        @PutMapping(
        path = "/profile/update",
        consumes = "application/json", 
        produces = "application/json"
    )
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateProfile(@RequestBody UpdateNasabahRequestDTO request) {
        ApiResponse<UserResponseDTO> response = userService.updateProfile(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping(
        path = "/profile/change-password",
        produces = "application/json"
    )
    public ResponseEntity<ApiResponse<String>> changePassword(
            @RequestBody ChangePasswordRequestDTO request) {
        ApiResponse<String> response = userService.changePassword(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping(
        path = "/admin/nasabah/{userId}/delete",
        produces = "application/json"
    )
    public ResponseEntity<ApiResponse<String>> deleteNasabah(@PathVariable UUID userId) {
        ApiResponse<String> response = userService.deleteNasabah(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
