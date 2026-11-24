package com.kdt.fridge.global.response;

import org.springframework.http.HttpStatus;

public record ApiResponse<T>(int status, String message, T data) {

    public static <T> ApiResponse<T> of(HttpStatus status, String message, T data) {
        return new ApiResponse<>(status.value(), message, data);
    }
}