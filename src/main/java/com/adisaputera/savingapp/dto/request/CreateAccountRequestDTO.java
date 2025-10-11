package com.adisaputera.savingapp.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateAccountRequestDTO {
    @JsonProperty("user_id")
    @NotBlank(message = "User ID is required")
    private String userId;
}
