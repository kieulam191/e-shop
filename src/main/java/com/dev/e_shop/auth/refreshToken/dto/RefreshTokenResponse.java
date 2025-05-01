package com.dev.e_shop.auth.refreshToken.dto;

public record RefreshTokenResponse(
        String newToken,
        String refreshToken
) { }
