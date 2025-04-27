package com.dev.e_shop.auth;

import com.dev.e_shop.user.User;
import com.dev.e_shop.user.UserDetail;
import com.dev.e_shop.user.role.Roles;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
    @Mock
    private Clock clock;

    private JwtService jwtService;

    private final Instant fixedNow = Instant.parse("2025-04-25T04:00:00Z");

    @BeforeEach
    void setUp() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application-test.properties")) {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String fakeSecretKey = properties.getProperty("jwt.secret");

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
}