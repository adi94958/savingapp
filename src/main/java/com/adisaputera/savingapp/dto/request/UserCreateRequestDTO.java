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
public class UserCreateRequestDTO {
    @JsonProperty("full_name")
    @NotBlank(message = "Full name is required")
    private String fullName;

    @JsonProperty("email")
    @Email
    @NotBlank(message = "Email is required")
    private String email;

    @JsonProperty("password")
    @NotBlank(message = "Password is required")
    private String password;

    @JsonProperty("role")
    @NotBlank(message = "Role is required")
    private String role;

    @JsonProperty("address")
    private String address;
    
    @JsonProperty("phone")
    private String phone;
}
