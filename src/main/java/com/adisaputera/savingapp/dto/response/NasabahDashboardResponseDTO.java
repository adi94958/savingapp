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
public class NasabahDashboardResponseDTO {
    
    @JsonProperty("total_deposit")
    private Long totalDeposit;
    
    @JsonProperty("total_withdraw")
    private Long totalWithdraw;
    
    @JsonProperty("total_balance")
    private Long totalBalance;

    @JsonProperty("total_accounts")
    private Integer totalAccounts;
    
    @JsonProperty("total_accounts_active")
    private Integer totalAccountsActive;

    @JsonProperty("total_accounts_inactive")
    private Integer totalAccountInActive;
}