package com.adisaputera.savingapp.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.adisaputera.savingapp.dto.message.ApiResponse;
import com.adisaputera.savingapp.dto.request.CreateTransactionRequestDTO;
import com.adisaputera.savingapp.dto.response.TransactionResponseDTO;
import com.adisaputera.savingapp.service.TransactionService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping(
        path = "/staff/transaction/{accountCode}/list", 
        produces = "application/json"
    )
    public ResponseEntity<ApiResponse<List<TransactionResponseDTO>>> getTransactionByAccount(
            @PathVariable String accountCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int perPage,
            @RequestParam(defaultValue = "asc") String sortDirection, 
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String keyword) {
        ApiResponse<List<TransactionResponseDTO>> response = transactionService.getTransactionByAccountCode(page, perPage, accountCode, sortDirection, sortBy,from, to, keyword);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(
        path = "/staff/transaction/create",
        consumes = "application/json",
        produces = "application/json"
    )
    public ResponseEntity<ApiResponse<TransactionResponseDTO>> createTransaction(
            @Valid @RequestBody CreateTransactionRequestDTO request) {
        ApiResponse<TransactionResponseDTO> response = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
