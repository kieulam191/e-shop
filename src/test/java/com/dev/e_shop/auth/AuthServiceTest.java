package com.dev.e_shop.auth;

import com.dev.e_shop.auth.refreshToken.RefreshToken;
import com.dev.e_shop.auth.refreshToken.RefreshTokenService;
import com.dev.e_shop.auth.refreshToken.dto.RefreshTokenRequest;
import com.dev.e_shop.auth.refreshToken.dto.RefreshTokenResponse;
import com.dev.e_shop.auth.refreshToken.exception.InvalidRefreshTokenException;
import com.dev.e_shop.exception.custom.AlreadyResourceException;
import com.dev.e_shop.user.User;
import com.dev.e_shop.user.UserDetail;
import com.dev.e_shop.user.UserRepository;
import com.dev.e_shop.user.dto.RegisterRequest;
import com.dev.e_shop.user.dto.LoginRequest;
import com.dev.e_shop.user.dto.LoginResponse;
import com.dev.e_shop.user.dto.RegisterResponse;
import com.dev.e_shop.user.mapper.UserMapper;
import com.dev.e_shop.user.role.Roles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    AuthService authService;

    @Mock
    UserRepository userRepository;

    @Mock
    UserMapper userMapper;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtService jwtService;

    @Mock
    RefreshTokenService refreshTokenService;

    @Mock
    private AuthenticationManager authenticationManager;


    @Test
    void register_withValidEmail_returnsUserResponse() {
        String rawPassword = "abc123";
        String encodedPassword = "$2a$10$encodedPasswordExample";

        RegisterRequest createUserRequest = new RegisterRequest("test@gmail.com", rawPassword);

        User toUser = new User();
        toUser.setEmail("test@gmail.com");
        toUser.setPassword(rawPassword);
        toUser.setRole(Roles.USER);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("test@gmail.com");
        savedUser.setPassword(encodedPassword);
        savedUser.setRole(Roles.USER);

        RegisterResponse userResponse = new RegisterResponse(
                "test@gmail.com",
                "USER");

        given(this.userRepository.existsByEmail("test@gmail.com")).willReturn(false);
        given(this.userMapper.toUser(createUserRequest)).willReturn(toUser);
        given(this.passwordEncoder.encode(rawPassword)).willReturn(encodedPassword);
        given(this.userRepository.save(any(User.class))).willReturn(savedUser);
        given(this.userMapper.toUserResponse(savedUser)).willReturn(userResponse);

        // when
        RegisterResponse actual = this.authService.register(createUserRequest);

        // then
        assertNotNull(actual);
        assertEquals("test@gmail.com", actual.email());
        assertEquals(Roles.USER.name(), actual.role());

        verify(this.userRepository).existsByEmail("test@gmail.com");
        verify(this.userMapper).toUser(createUserRequest);
        verify(this.passwordEncoder).encode(rawPassword);
        verify(this.userRepository).save(any(User.class));
        verify(this.userMapper).toUserResponse(savedUser);
    }

    @Test
    void register_withExistingEmail_throwsAlreadyResourceException() {
        //given
        String rawPassword = "abc123";
        RegisterRequest createUserRequest = new RegisterRequest("test@gmail.com", rawPassword);

        given(this.userRepository.existsByEmail("test@gmail.com")).willReturn(true);

        // when
        assertThrows(AlreadyResourceException.class, () -> {
            this.authService.register(createUserRequest);
        });

        // then
        verify(this.userRepository).existsByEmail("test@gmail.com");
    }

    @Test
    void login_withValidCredential_returnUserResponse() {
        // given
        String email = "lam@gmail.com";
        String password = "123456";
        String token = "valid_token";
        String refresh_token = "valid_token";

        LoginRequest request = new LoginRequest (email, password);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refresh_token);

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("USER"));
        User user = new User();
        user.setEmail("lam@gmail.com");
        user.setRole(Roles.USER);

        UserDetail userDetail = new UserDetail(user);

        Authentication authenticated = new UsernamePasswordAuthenticationToken(
                userDetail, null, authorities);


        given(authenticationManager.authenticate(any(Authentication.class))).willReturn(authenticated);
        given(jwtService.generateToken(any(UserDetail.class))).willReturn(token);
        given(refreshTokenService.create("lam@gmail.com")).willReturn(refreshToken);
        // when
        LoginResponse response = authService.login(request);

        // then
        assertEquals(email, response.email());
        assertEquals("USER", response.role());
    }

    @Test
    void login_withWrongCredential_throwsBadCredentialsException() {
        // given
        String email = "lam@gmail.com";
        String password = "123456";
        LoginRequest request = new LoginRequest(email, password);

        given(authenticationManager.authenticate(any(Authentication.class)))
                .willThrow(BadCredentialsException.class);
        // when
        assertThrows(BadCredentialsException.class, () -> {
            LoginResponse response = authService.login(request);
        });
    }

    @Test
    void refresh_withValidToken_returnsRefreshTokenResponse() {
        //given
        String token = "valid-token";
        RefreshTokenRequest body = new RefreshTokenRequest(token);


        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setUserId(1L);
        refreshToken.setToken(token);

        User user = new User();
        user.setId(1L);
        given(this.refreshTokenService.verifyRefreshToken("valid-token"))
                .willReturn(refreshToken);

        given(this.userRepository.findById(1L))
                .willReturn(Optional.of(user));

        given(this.jwtService.generateToken(any(UserDetail.class)))
                .willReturn("new-token");
        //when
        RefreshTokenResponse actual = this.authService.refresh(body);
        //then
        assertThat(actual).isNotNull();
        assertEquals("new-token",actual.newToken());
        assertEquals(token,actual.refreshToken());
    }

    @Test
    void refresh_withExpiryToken_returnsInvalidRefreshTokenException() {
        //given
        String token = "expiry-token";
        RefreshTokenRequest body = new RefreshTokenRequest(token);

        given(this.refreshTokenService.verifyRefreshToken("expiry-token"))
                .willThrow(InvalidRefreshTokenException.class);

        //when
        assertThrows(InvalidRefreshTokenException.class, () -> {
            RefreshTokenResponse actual = this.authService.refresh(body);
        });
    }
}