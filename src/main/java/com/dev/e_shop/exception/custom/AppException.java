package com.dev.e_shop.exception.custom;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final int status;
    private final String errorDetail;
    private final String message;

    public AppException(String detail, int status, String message) {
        super(message);
        this.status = status;
        this.errorDetail = detail;
        this.message = message;
    }
}
