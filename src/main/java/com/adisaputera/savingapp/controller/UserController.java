package com.adisaputera.savingapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.adisaputera.savingapp.dto.message.ApiResponse;
import com.adisaputera.savingapp.dto.request.ChangePasswordRequestDTO;
import com.adisaputera.savingapp.dto.request.UserCreateRequestDTO;
import com.adisaputera.savingapp.dto.request.UserUpdateRequestDTO;
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
@RequestMapping("/api/staff/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping(
        path = "/create", 
        produces = "application/json"
    )
    public ResponseEntity<ApiResponse<UserResponseDTO>> createUser(@Valid @RequestBody UserCreateRequestDTO request) {
        ApiResponse<UserResponseDTO> response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(
        path = "/list", 
        produces = "application/json"
    )
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getUserList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int perPage,
            @RequestParam(defaultValue = "fullName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection, 
            @RequestParam(required = false) String keyword) {
        ApiResponse<List<UserResponseDTO>> response = userService.getUserList(page, perPage, sortBy, sortDirection, keyword);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(
        path = "/{userId}/detail", 
        produces = "application/json"
    )
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(@PathVariable UUID userId) {
        ApiResponse<UserResponseDTO> response = userService.getUserById(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    @PutMapping(
        path = "/{userId}/update", 
        consumes = "application/json", 
        produces = "application/json"
    )
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUser(@PathVariable UUID userId, @RequestBody UserUpdateRequestDTO request) {
        ApiResponse<UserResponseDTO> response = userService.updateUser(userId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping(
        path = "/{userId}/password",
        produces = "application/json"
    )
    public ResponseEntity<ApiResponse<String>> changePassword(
            @PathVariable UUID userId,
            @RequestBody ChangePasswordRequestDTO request) {
        ApiResponse<String> response = userService.changePassword(userId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping(
        path = "/{userId}/delete",
        produces = "application/json"
    )
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable UUID userId) {
        ApiResponse<String> response = userService.deleteUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
