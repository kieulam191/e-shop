package com.dev.e_shop.exception;

public class CartItemNotFoundException extends RuntimeException{
    public CartItemNotFoundException(String msg) {
        super(msg);
    }
}
