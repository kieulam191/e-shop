package com.dev.e_shop.exception.custom;

import com.dev.e_shop.exception.status.ErrorStatus;

public class InvalidHttpStatusException extends AppException{
    public InvalidHttpStatusException(String errorDetail) {
        super(errorDetail, 400, ErrorStatus.INVALID_HTTP_STATUS);
    }
}
