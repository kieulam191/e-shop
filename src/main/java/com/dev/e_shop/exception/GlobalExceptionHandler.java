package com.dev.e_shop.exception;

import com.dev.e_shop.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidHttpStatusException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidHttpStatus(InvalidHttpStatusException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(400, ex.getMessage(), null));
    }
}
