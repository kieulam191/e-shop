package com.dev.e_shop.auth;

import com.dev.e_shop.user.User;
import com.dev.e_shop.user.UserDetail;
import com.dev.e_shop.user.role.Roles;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
    @Mock
    private Clock clock;

    private JwtService jwtService;

    private final Instant fixedNow = Instant.parse("2025-04-25T04:00:00Z");

    String fakeSecretKey;

    @BeforeEach
    void setUp() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application-test.properties")) {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        fakeSecretKey = properties.getProperty("jwt.secret");

        when(clock.instant()).thenReturn(fixedNow);
        lenient().when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        jwtService = new JwtService(clock);
        ReflectionTestUtils.setField(jwtService, "secretKey", fakeSecretKey);
    }

    @Test
    void generateToken_withValidCredential_returnsToken() {
        //given
        User user = new User();
        user.setId(1L);
        user.setEmail("test@gmail.com");
        user.setRole(Roles.USER);

        UserDetail userDetail = new UserDetail(user);
        //when
        String token = this.jwtService.generateToken(userDetail);

        //then
        assertNotNull(token);

        Claims claims = jwtService.extractClaims(token);

        assertEquals("test@gmail.com", claims.getSubject());
        assertEquals("ROLE_USER", claims.get("role", String.class));
        assertEquals(Instant.parse("2025-04-25T04:00:00Z"), claims.getIssuedAt().toInstant());
        assertEquals(Instant.parse("2025-04-25T05:00:00Z"), claims.getExpiration().toInstant());

    }

    @Test
    void generateToken_withChangedToken_throwsSignatureException() {
        //given
        User user = new User();
        user.setId(1L);
        user.setEmail("test@gmail.com");
        user.setRole(Roles.USER);

        UserDetail userDetail = new UserDetail(user);

        Instant fixedNow = Instant.parse("2025-04-25T04:00:00Z");
        lenient().when(clock.instant()).thenReturn(fixedNow);
        //when

        String token = this.jwtService.generateToken(userDetail);

        String changedToken = token + "X";
        //then
        assertNotNull(changedToken);

        assertThrows(SignatureException.class, () -> {
            jwtService.extractClaims(changedToken);
        });
    }

    @Test
    void extractClaims_withValidClaims_returnsClaimInfo() {
        //given
        byte[] keyBytes = Base64.getDecoder().decode(fakeSecretKey);
        SecretKey secretKey1 = Keys.hmacShaKeyFor(keyBytes);

        long issuedAtMillis = 1745870400000L;
        long expirationMillis = 1745874000000L;

        String token = Jwts.builder()
                .subject("test@gmail.com")
                .claim("role", Roles.USER.name())
                .issuedAt(new Date(issuedAtMillis))
                .expiration(new Date(expirationMillis))
                .signWith(secretKey1)
                .compact();
        //when
        Claims actual = this.jwtService.extractClaims(token);
        //then
        assertNotNull(actual);
        assertEquals("test@gmail.com", actual.getSubject());
        assertEquals(Roles.USER.name(), actual.get("role"));
        assertEquals(issuedAtMillis, actual.getIssuedAt().getTime());
        assertEquals(expirationMillis, actual.getExpiration().getTime());
    }
}