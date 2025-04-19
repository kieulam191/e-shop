package com.dev.e_shop.exception;

import com.dev.e_shop.dto.ApiResponse;
import com.dev.e_shop.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.*;


@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidHttpStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidHttpStatus(InvalidHttpStatusException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        400,
                        "Invalid http status code",
                        ex.getMessage(),
                        null)
                );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleInvalidHttpStatus(
            NoResourceFoundException ex,
            HttpServletRequest request) {


        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        404,
                        "Page not found",
                        Arrays.asList("Endpoint " + request.getRequestURI() + " does not exist"),
                        request.getRequestURI()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handlerNotFoundException(NotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse<>(
                        404,
                        "Resource not found",
                        Collections.singleton(ex.getMessage()),
                        request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleUniqueConstraintException(
            MethodArgumentNotValidException e,
            HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();

        e.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                        "Validation failed for one or more fields.",
                        errors,
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(DataIntegrityViolationException .class)
    public ResponseEntity<ErrorResponse> handleSqlConstraintViolation(
            DataIntegrityViolationException e,
            HttpServletRequest request) {

        Throwable rootCause = getRootCause(e);
        Set<String> messages = getMessageFromSqlViolation(rootCause);

        return ResponseEntity.status(409)
                .body(new ErrorResponse(
                        409,
                        "Database constraint violation",
                        messages,
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportException(
            HttpRequestMethodNotSupportedException e,
            HttpServletRequest request) {

       String message = String.format("Request method '%s' is not supported for this endpoint.", e.getMethod());

        return ResponseEntity.status(405)
                .body(new ErrorResponse(
                        405,
                        "HTTP method not supported",
                        Arrays.asList(message),
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

    private Set<String> getMessageFromSqlViolation(Throwable rootCause) {
        String message = rootCause.getMessage();
        Set<String> msgSet = new HashSet<>();
        if (message.contains("Unique") || message.contains("Duplicate")) {
            msgSet.add("Duplicate data detected. Please check and try again.");
        }
        if (message.contains("null")) {
            msgSet.add("Required information is missing.");
        } else if (message.contains("foreign key") || message.contains("constraint")) {
            msgSet.add("Operation failed due to data constraint violation.");
        }

        return msgSet;
    }
}
