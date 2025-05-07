package com.dev.e_shop.auth.refreshToken.exception;

import com.dev.e_shop.exception.custom.AppException;
import com.dev.e_shop.exception.status.ErrorStatus;

public class InvalidRefreshTokenException extends AppException {
    public InvalidRefreshTokenException(String errorDetail) {
        super(errorDetail, 401, ErrorStatus.INVALID_REFRESH_TOKEN);
    }
}
