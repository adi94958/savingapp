package com.adisaputera.savingapp.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChangePasswordRequestDTO {
    @JsonProperty("old_password")
    private String oldPassword;
    
    @JsonProperty("new_password")
    private String newPassword;
}
