package com.dev.e_shop.exception;

import com.dev.e_shop.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidHttpStatusException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidHttpStatus(InvalidHttpStatusException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(400, ex.getMessage(), null));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidHttpStatus(
            NoResourceFoundException ex,
            HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(404, "Page not found", request.getRequestURI()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handlerNotFoundException(NotFoundException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(404, ex.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleUniqueConstraintException(
            MethodArgumentNotValidException e,
            HttpServletRequest request) {

        String errorMessages = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));


        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(HttpStatus.BAD_REQUEST.value(),
                         errorMessages,
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(DataIntegrityViolationException .class)
    public ResponseEntity<ApiResponse> handleSqlConstraintViolation(
            DataIntegrityViolationException e,
            HttpServletRequest request) {

        Throwable rootCause = getRootCause(e);
        String message = getMessageFromSqlViolation(rootCause);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(HttpStatus.BAD_REQUEST.value(),
                        message,
                        request.getRequestURI()
                ));
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        while (cause != null && cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause != null ? cause : throwable;
    }

    private String getMessageFromSqlViolation(Throwable rootCause) {
        String message = rootCause.getMessage();
        if (message.contains("Unique") || message.contains("Duplicate")) {
            message = "Duplicate data detected. Please check and try again.";
        } else if (message.contains("null")) {
            message = "Required information is missing.";
        } else if (message.contains("foreign key") || message.contains("constraint")) {
            message = "Operation failed due to data constraint violation.";
        }

        return message;
    }


}
