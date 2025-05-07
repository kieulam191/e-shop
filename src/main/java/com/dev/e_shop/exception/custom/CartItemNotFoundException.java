package com.dev.e_shop.exception.custom;

import com.dev.e_shop.exception.status.ErrorStatus;

public class CartItemNotFoundException extends AppException{
    public CartItemNotFoundException(String errorDetail) {
        super(errorDetail, 400, ErrorStatus.CART_ITEM_NOT_FOUND);
    }
}
