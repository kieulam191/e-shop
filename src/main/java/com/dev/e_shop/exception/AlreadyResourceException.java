package com.dev.e_shop.exception;

public class AlreadyResourceException extends RuntimeException {
    public AlreadyResourceException(String msg) {
        super(msg);
    }
}
