package com.adisaputera.savingapp.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.adisaputera.savingapp.model.TypeTransactionEnum;
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
public class TransactionResponseDTO {
    @JsonProperty("transaction_id")
    private UUID transactionId;

    @JsonProperty("account_code")
    private AccountResponseDTO account;

    @JsonProperty("type")
    private TypeTransactionEnum type;

    @JsonProperty("amount")
    private Long amount;

    @JsonProperty("note")
    private String note;

    @JsonProperty("occurred_at")
    private LocalDateTime occurredAt;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}  
