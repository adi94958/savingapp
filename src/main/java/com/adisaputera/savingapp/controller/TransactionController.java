package com.adisaputera.savingapp.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.function.EntityResponse;

import com.adisaputera.savingapp.dto.message.ApiResponse;
import com.adisaputera.savingapp.dto.response.TransactionResponseDTO;
import com.adisaputera.savingapp.dto.response.UserResponseDTO;
import com.adisaputera.savingapp.service.TransactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/nasabah/transaction")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping(
        path = "/list", 
        produces = "application/json"
    )
    public EntityResponse<ApiResponse<List<UserResponseDTO>>> getTransactionByAccount(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int perPage,
            @RequestParam(required = true) String accountCode,
            @RequestParam(defaultValue = "asc") String sortDirection, 
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String keyword) {
        ApiResponse<List<TransactionResponseDTO>> response = transactionService.getTransactionByAccount(page, perPage, accountCode, sortDirection, sortBy,from, to, keyword);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
