package com.adisaputera.savingapp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountMeResponseDTO {
    @JsonProperty("account_code")
    private String accountCode;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("total_deposit")
    private Long totalDeposit;

    @JsonProperty("total_withdraw")
    private Long totalWithdraw;

    @JsonProperty("balance")
    private Long balance;

    @JsonProperty("created_at")
    private String createdAt;
}
