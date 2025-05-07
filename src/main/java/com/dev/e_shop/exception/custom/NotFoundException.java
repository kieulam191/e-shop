package com.dev.e_shop.exception.custom;

import com.dev.e_shop.exception.status.ErrorStatus;

public class NotFoundException extends AppException {
    public NotFoundException(String errorDetail) {
        super(errorDetail, 404, ErrorStatus.RES_NOT_FOUND);
    }
}
