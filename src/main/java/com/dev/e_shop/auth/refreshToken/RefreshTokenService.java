package com.dev.e_shop.auth.refreshToken;

import com.dev.e_shop.auth.refreshToken.exception.InvalidRefreshTokenException;
import com.dev.e_shop.exception.NotFoundException;
import com.dev.e_shop.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
public class RefreshTokenService {
    private final int EXPIRY_DAYS = 7;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final Clock clock;

    public RefreshTokenService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, Clock clock) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.clock = clock;
    }


    @Transactional
    public RefreshToken create(String email) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    RefreshToken refreshToken = new RefreshToken();
                    refreshToken.setUserId(user.getId());
                    refreshToken.setToken(generateToken());
                    refreshToken.setExpiryDate(clock.instant().plus(EXPIRY_DAYS, ChronoUnit.DAYS));

                    return this.refreshTokenRepository.save(refreshToken);
                })
                .orElseThrow(() -> new NotFoundException("Email not found"));
    }


    @Transactional
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));


        if (refreshToken.getExpiryDate().isBefore(clock.instant())) {
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidRefreshTokenException("Refresh token expired");
        }

        return refreshToken;
    }

    @Transactional
    public void deleteRefreshToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }


    private String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
