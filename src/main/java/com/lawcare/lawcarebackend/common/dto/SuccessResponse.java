package com.lawcare.lawcarebackend.common.dto;

import java.time.LocalDateTime;

public record SuccessResponse<T>(
    LocalDateTime timestamp,
    int status,
    String message,
    T data,
    String path
) {
    public static <T> SuccessResponse<T> of(int status, String message, T data, String path) {
        return new SuccessResponse<>(LocalDateTime.now(), status, message, data, path);
    }
}
