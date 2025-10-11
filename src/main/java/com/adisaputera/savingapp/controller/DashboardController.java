package com.adisaputera.savingapp.controller;

import com.adisaputera.savingapp.dto.message.ApiResponse;
import com.adisaputera.savingapp.dto.response.AdminDashboardResponseDTO;
import com.adisaputera.savingapp.dto.response.NasabahDashboardResponseDTO;
import com.adisaputera.savingapp.service.DashboardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping(
        path = "/admin/dashboard",
        produces = "application/json"
    )
    public ResponseEntity<ApiResponse<AdminDashboardResponseDTO>> getAdminDashboard() {
        ApiResponse<AdminDashboardResponseDTO> response = dashboardService.getAdminDashboard();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(
        path = "/nasabah/dashboard",
        produces = "application/json"
    )
    public ResponseEntity<ApiResponse<NasabahDashboardResponseDTO>> getNasabahDashboard(
            @RequestParam(defaultValue = "") String accountCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Boolean status) {
        
        ApiResponse<NasabahDashboardResponseDTO> response = dashboardService.getNasabahDashboard(accountCode, from, to, status);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}