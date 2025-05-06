package com.dev.e_shop.auth.refreshToken;

import com.dev.e_shop.auth.refreshToken.exception.InvalidRefreshTokenException;
import com.dev.e_shop.exception.custom.NotFoundException;
import com.dev.e_shop.user.User;
import com.dev.e_shop.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @InjectMocks
    RefreshTokenService refreshTokenService;

    @Mock
    RefreshTokenRepository refreshTokenRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    Clock clock;

    @Test
    void create_withValidEmail_returnsRefreshToken() {
       //given
       String email = "test@gmail.com";
       String token = "valid-token";
       when(clock.instant()).thenReturn(Instant.parse("2025-04-25T04:00:00Z"));
       Instant expiryDate = clock.instant().plus(7, ChronoUnit.DAYS);

       User user = new User();
       user.setId(1L);

       RefreshToken refreshToken = new RefreshToken();
       refreshToken.setUserId(1L);
       refreshToken.setToken(token);
       refreshToken.setExpiryDate(expiryDate);

        RefreshToken savedRefreshToken = new RefreshToken();
        savedRefreshToken.setId(1L);
        savedRefreshToken.setToken(token);
        savedRefreshToken.setUserId(1L);
        savedRefreshToken.setExpiryDate(expiryDate);

        given(this.userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(this.refreshTokenRepository.save(any(RefreshToken.class))).willReturn(savedRefreshToken);

        //when
        RefreshToken actual = this.refreshTokenService.create(email);

        //then
        assertNotNull(actual);
        assertEquals(1L, actual.getId());
        assertEquals(token, actual.getToken());
        assertEquals(1L, actual.getUserId());
        assertEquals("valid-token", actual.getToken());
    }

    @Test
    void create_withNotExistingEmail_throwsNotFoundException() {
        //given
        String email = "test@gmail.com";

        given(this.userRepository.findByEmail(email))
                .willReturn(Optional.empty());

        //when
        assertThrows(NotFoundException.class, () -> {
            RefreshToken actual = this.refreshTokenService.create(email);
        });

        //then
        verify(this.userRepository, times(1)).findByEmail(email);
    }

    @Test
    void verifyRefreshToken_withValidToken_returnRefreshToken() {
        String token = "valid-token";
        when(clock.instant()).thenReturn(Instant.parse("2025-04-25T04:00:00Z"));

        RefreshToken savedRefreshToken = new RefreshToken();
        savedRefreshToken.setId(1L);
        savedRefreshToken.setExpiryDate(Instant.parse("2025-04-25T04:00:00Z"));
        savedRefreshToken.setToken(token);

        given(this.refreshTokenRepository.findByToken(token))
                .willReturn(Optional.of(savedRefreshToken));

        //when
        RefreshToken actual = this.refreshTokenService.verifyRefreshToken(token);

        //then
        assertNotNull(actual);
        assertEquals(1L,actual.getId());
    }

    @Test
    void verifyRefreshToken_withExpiryToken_removesRefreshToken() {
        String token = "valid-token";
        when(clock.instant()).thenReturn(Instant.parse("2025-04-30T00:00:00Z"));

        RefreshToken savedRefreshToken = new RefreshToken();
        savedRefreshToken.setId(1L);
        savedRefreshToken.setExpiryDate(Instant.parse("2025-04-25T04:00:00Z"));
        savedRefreshToken.setToken(token);

        given(this.refreshTokenRepository.findByToken(token))
                .willReturn(Optional.of(savedRefreshToken));
        doNothing().when(this.refreshTokenRepository).delete(savedRefreshToken);
        //when
        InvalidRefreshTokenException exception = assertThrows(InvalidRefreshTokenException.class, () -> {
            this.refreshTokenService.verifyRefreshToken(token);
        });

        //then
        assertEquals("Refresh token expired", exception.getErrorDetail());
    }

    @Test
    void verifyRefreshToken_withInvalidToken_throwsRuntimeException() {
        // Given
        String token = "invalid-token";

        given(this.refreshTokenRepository.findByToken(token))
                .willReturn(Optional.empty());

        // When & Then
        InvalidRefreshTokenException exception = assertThrows(InvalidRefreshTokenException.class, () -> {
            this.refreshTokenService.verifyRefreshToken(token);
        });

        assertEquals("Invalid refresh token", exception.getErrorDetail());
    }

    @Test
    void deleteRefreshToken_whenLogout_removesRefreshToken() {
        String token = "valid-token";

        willDoNothing().given(this.refreshTokenRepository).deleteByToken(token);

        //when
        this.refreshTokenService.deleteRefreshToken(token);

        verify(this.refreshTokenRepository, times(1)).deleteByToken(token);
    }
}