package com.dev.e_shop.dto;

import jakarta.validation.constraints.NotBlank;

public record ErrorResponse<T> (
        int status,
        @NotBlank String message,
        T errors,
        String path
) { }
