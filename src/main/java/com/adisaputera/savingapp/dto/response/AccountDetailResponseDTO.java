package com.adisaputera.savingapp.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountDetailResponseDTO {
    @JsonProperty("account_code")
    private String accountCode;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    @JsonProperty("balance")
    private Double balance;
    
    @JsonProperty("total_deposit")
    private Double totalDeposit;
    
    @JsonProperty("total_withdraw")
    private Double totalWithdraw;
    
    @JsonProperty("created_at")
    private String createdAt;
    
    @JsonProperty("user")
    private UserResponseDTO user;
}