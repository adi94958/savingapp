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
import com.adisaputera.savingapp.dto.request.ChangeAccountStatusRequestDTO;
import com.adisaputera.savingapp.dto.request.CreateAccountRequestDTO;
import com.adisaputera.savingapp.dto.response.AccountDetailResponseDTO;
import com.adisaputera.savingapp.dto.response.AccountListResponseDTO;
import com.adisaputera.savingapp.dto.response.AccountMeResponseDTO;
import com.adisaputera.savingapp.service.AccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class AccountController {
    private final AccountService accountService;

    @PostMapping(
        path = "/admin/account/create", 
        produces = "application/json"
    )
    public ResponseEntity<ApiResponse<AccountListResponseDTO>> createAccountNasabah(@Valid @RequestBody CreateAccountRequestDTO request) {
        ApiResponse<AccountListResponseDTO> response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(
        path = "/admin/account/list", 
        produces = "application/json"
    )
    public ResponseEntity<ApiResponse<List<AccountListResponseDTO>>> getAccountList(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int perPage,
        @RequestParam(defaultValue = "asc") String sortDirection,
        @RequestParam(defaultValue = "createdAt") String sortBy, //createdAt, balance, totalDeposit, totalWithdraw, accountCode
        @RequestParam(required = false) UUID userId,
        @RequestParam(required = false) String keyword) { // keyword searches by accountCode
        ApiResponse<List<AccountListResponseDTO>> response = accountService.getAccountList(page, perPage, sortDirection, sortBy, userId, keyword);
        return ResponseEntity.status(HttpStatus.OK).body(response);
        }

    @PatchMapping(
        path = "/admin/account/update/{accountCode}/status", 
        produces = "application/json"
    )
    public ResponseEntity<ApiResponse<String>> updateAccountStatus(
        @PathVariable String accountCode,
        @RequestBody ChangeAccountStatusRequestDTO request
    ) {
        request.setAccountCode(accountCode);
        ApiResponse<String> response = accountService.updateAccountStatus(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(
        path = "/admin/account/detail/{accountCode}", 
        produces = "application/json"
    )
    public ResponseEntity<ApiResponse<AccountDetailResponseDTO>> getAccountDetail(
        @PathVariable String accountCode
    ) {
        ApiResponse<AccountDetailResponseDTO> response = accountService.getAccountDetail(accountCode);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping(
        path = "/admin/account/delete/{accountCode}/delete", 
        produces = "application/json"
    )
    public ResponseEntity<ApiResponse<String>> deleteAccount(
        @PathVariable String accountCode
    ) {
        ApiResponse<String> response = accountService.deleteAccount(accountCode);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(
        path = "/nasabah/account/me", 
        produces = "application/json"
    )
    public ResponseEntity<ApiResponse<List<AccountMeResponseDTO>>> getAccountMe(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int perPage,
        @RequestParam(defaultValue = "asc") String sortDirection,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(required = false) String keyword
    ) {
        ApiResponse<List<AccountMeResponseDTO>> response = accountService.getAccountMe(page, perPage, sortDirection, sortBy, keyword);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
