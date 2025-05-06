package com.dev.e_shop.exception;

import com.dev.e_shop.dto.ErrorResponse;
import com.dev.e_shop.exception.custom.AppException;
import com.dev.e_shop.exception.custom.InvalidHttpStatusException;
import com.dev.e_shop.exception.status.ErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.*;


@RestControllerAdvice
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
    public ResponseEntity<ErrorResponse> handlePageNotFound(
            NoResourceFoundException ex,
            HttpServletRequest request) {


        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        404,
                        ErrorStatus.PAGE_NOT_FOUND,
                        Arrays.asList("Endpoint " + request.getRequestURI() + " does not exist"),
                        request.getRequestURI()));
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse<>(ex.getStatus(),
                        ex.getMessage(),
                        Collections.singleton(ex.getErrorDetail()),
                        request.getRequestURI()));
    }

    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleAuthExceptions(
            AuthenticationException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse<>(
                        401,
                        ErrorStatus.USERNAME_NOT_FOUND,
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
                        ErrorStatus.UNIQUE_CONSTRAINT,
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
                        ErrorStatus.SQL_CONSTRAINT,
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
                        ErrorStatus.METHOD_NOT_SUPPORT,
                        Arrays.asList(message),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {
        return ResponseEntity.status(500).body(new ErrorResponse(
                500,
                ErrorStatus.INTERNAL_SERVER,
                Arrays.asList("An unexpected error occurred"),
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
