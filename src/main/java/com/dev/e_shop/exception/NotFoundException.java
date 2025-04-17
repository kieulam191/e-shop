package com.dev.e_shop.exception;

public class NotFoundException extends RuntimeException{
    public NotFoundException(String msg) {
        super(msg);
    }
}
