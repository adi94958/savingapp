package com.adisaputera.savingapp.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChangeAccountStatusRequestDTO {
    @NotNull(message = "Status is required")
    @JsonProperty("is_active")
    private Boolean isActive;
   
    @JsonProperty("account_code")
    private String accountCode;
}
