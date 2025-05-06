package com.dev.e_shop.exception.custom;

import com.dev.e_shop.exception.status.ErrorStatus;

public class AlreadyResourceException extends AppException {
    public AlreadyResourceException(String errorDetail) {
        super(errorDetail, 409, ErrorStatus.ALREADY_RES);
    }
}
