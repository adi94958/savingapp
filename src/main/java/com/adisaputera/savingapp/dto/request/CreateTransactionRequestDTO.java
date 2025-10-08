package com.adisaputera.savingapp.dto.request;

import com.adisaputera.savingapp.model.TypeTransactionEnum;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CreateTransactionRequestDTO {
    @JsonProperty("accountCode")
    @NotBlank(message = "Account code is required")
    private String accountCode;

    @JsonProperty("type")
    @NotNull(message = "Transaction type is required")
    private TypeTransactionEnum type;

    @JsonProperty("amount")
    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be greater than 0")
    private Long amount;

    @JsonProperty("note")
    @Size(max = 200, message = "Note cannot exceed 200 characters")
    private String note;
}