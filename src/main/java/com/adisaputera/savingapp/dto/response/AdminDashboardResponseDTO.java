package com.adisaputera.savingapp.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AdminDashboardResponseDTO {
    
    @JsonProperty("total_nasabah")
    private Long totalNasabah;
    
    @JsonProperty("total_account_active")
    private Long totalAccountActive;
    
    @JsonProperty("total_account_inactive")
    private Long totalAccountInactive;
    
    @JsonProperty("total_deposit")
    private Long totalDeposit;
    
    @JsonProperty("total_withdraw")
    private Long totalWithdraw;
    
    @JsonProperty("total_balance")
    private Long totalBalance;
}