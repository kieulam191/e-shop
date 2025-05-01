package com.dev.e_shop.auth.refreshToken.exception;

public class InvalidRefreshTokenException extends RuntimeException{
    public InvalidRefreshTokenException(String msg) {
        super(msg);
    }
}
