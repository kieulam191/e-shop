package com.dev.e_shop.dto;

import com.dev.e_shop.exception.custom.InvalidHttpStatusException;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;

public record ApiResponse<T>(
        int status,
        @NotBlank String message,
        T data
) {
    public ApiResponse {
        if (HttpStatus.resolve(status) == null) {
            throw new InvalidHttpStatusException("Invalid HTTP status code: " + status);
        }
    }
}
