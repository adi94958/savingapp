package com.adisaputera.savingapp.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.adisaputera.savingapp.dto.message.ApiResponse;
import com.adisaputera.savingapp.dto.request.AccountCreateRequestDTO;
import com.adisaputera.savingapp.dto.response.AccountResponseDTO;
import com.adisaputera.savingapp.service.AccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/staff/account")
@RequiredArgsConstructor
@Slf4j
public class AccountController {
    private final AccountService accountService;

    @PostMapping(
        path = "/create", 
        produces = "application/json"
    )
    public ResponseEntity<ApiResponse<AccountResponseDTO>> createAccountNasabah(@Valid @RequestBody AccountCreateRequestDTO request) {
        ApiResponse<AccountResponseDTO> response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(
        path = "/list", 
        produces = "application/json"
    )
    public ResponseEntity<ApiResponse<List<AccountResponseDTO>>> getAccountList(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int perPage,
        @RequestParam(defaultValue = "asc") String sortDirection,
        @RequestParam(required = false) UUID userId,
        @RequestParam(required = false) String keyword) {
        ApiResponse<List<AccountResponseDTO>> response = accountService.getAccountList(page, perPage, sortDirection, userId, keyword);
        return ResponseEntity.status(HttpStatus.OK).body(response);
        }

    @PatchMapping(
        path = "/update/{accountCode}/status", 
        produces = "application/json"
    )
    public ResponseEntity<ApiResponse<String>> updateAccountStatus(
        @PathVariable String accountCode,
        @RequestBody boolean status
    ) {
        ApiResponse<String> response = accountService.updateAccountStatus(accountCode, status);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping(
        path = "/delete/{accountCode}/delete", 
        produces = "application/json"
    )
    public ResponseEntity<ApiResponse<String>> deleteAccount(
        @PathVariable String accountCode
    ) {
        ApiResponse<String> response = accountService.deleteAccount(accountCode);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
