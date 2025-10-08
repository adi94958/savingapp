package com.adisaputera.savingapp.dto.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse <T> {
    @JsonProperty("status")
    private String status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private T data;

    @JsonProperty("metadata")
    private MetadataResponse metadata;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status("success")
                .message(message)
                .data(data)
                .metadata(null)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data, MetadataResponse metadata) {
        return ApiResponse.<T>builder()
                .status("success")
                .message(message)
                .data(data)
                .metadata(metadata)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return ApiResponse.<T>builder()
                .status("error")
                .message(message)
                .data(data)
                .metadata(null)
                .build();
    }

    public static ApiResponse<Object> error(String message, String details) {
        return ApiResponse.builder()
                .status("error")
                .message(message)
                .data(details)
                .metadata(null)
                .build();
    }
}