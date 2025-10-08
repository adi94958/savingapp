package com.adisaputera.savingapp.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginRequestDTO {
    @JsonProperty("email")
    @Email
    @NotBlank(message = "Email is required")
    private String email;

    @JsonProperty("password")
    @NotBlank(message = "Password is required")
    private String password;
}
