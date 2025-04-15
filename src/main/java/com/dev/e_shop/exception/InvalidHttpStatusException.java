package com.dev.e_shop.exception;

public class InvalidHttpStatusException extends RuntimeException{
    public InvalidHttpStatusException(String msg) {
        super(msg);
    }
}
