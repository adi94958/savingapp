package com.adisaputera.savingapp.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RefreshTokenRequestDTO {
    @JsonProperty("refresh_token")
    private String refreshToken;
}
