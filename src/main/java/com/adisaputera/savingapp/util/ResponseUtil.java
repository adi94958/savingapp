package com.adisaputera.savingapp.util;

import com.adisaputera.savingapp.dto.message.ApiResponse;
import com.adisaputera.savingapp.dto.message.MetadataResponse;

public class ResponseUtil {
    
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