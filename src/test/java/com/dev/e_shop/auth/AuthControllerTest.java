package com.dev.e_shop.auth;

import com.dev.e_shop.auth.refreshToken.dto.RefreshTokenRequest;
import com.dev.e_shop.auth.refreshToken.dto.RefreshTokenResponse;
import com.dev.e_shop.auth.refreshToken.exception.InvalidRefreshTokenException;
import com.dev.e_shop.exception.AlreadyResourceException;
import com.dev.e_shop.user.dto.LoginRequest;
import com.dev.e_shop.user.dto.LoginResponse;
import com.dev.e_shop.user.dto.RegisterRequest;
import com.dev.e_shop.user.dto.RegisterResponse;
import com.dev.e_shop.user.role.Roles;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @MockitoBean
    AuthService authService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void login_withValidInput_returnsLoginResponse() throws Exception {
        LoginRequest body = new LoginRequest("test@gmail.com", "testabc");
        String json = objectMapper.writeValueAsString(body);

        LoginResponse loginResponse = new LoginResponse(
                "test@gmail.com",
                Roles.USER.name(),
                "valid_token",
                "valid_refresh_token");


        given(this.authService.login(any(LoginRequest.class)))
                .willReturn(loginResponse);
        //when and then
        this.mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Login success"))
                .andExpect(jsonPath("$.data.email").value("test@gmail.com"))
                .andExpect(jsonPath("$.data.role").value(Roles.USER.name()))
                .andExpect(jsonPath("$.data.token").value("valid_token"));
    }

    @Test
    void login_withWrongCredentials_throwsBadCredentialsException() throws Exception {
        LoginRequest body = new LoginRequest("test@gmail.com", "testabc");
        String json = objectMapper.writeValueAsString(body);

        given(this.authService.login(any(LoginRequest.class)))
                .willThrow(BadCredentialsException.class);
        //when and then
        this.mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Bad credentials"))
                .andExpect(jsonPath("$.errors[0]").value("Wrong email or password"))
                .andExpect(jsonPath("$.path").value("/api/auth/login"));
    }

    @Test
    void login_withInValidInput_throwsBadRequestException() throws Exception {
        LoginRequest body = new LoginRequest("testgmail.com", "test");
        String json = objectMapper.writeValueAsString(body);

        given(this.authService.login(any(LoginRequest.class)))
                .willThrow(DataIntegrityViolationException.class);

        //when and then
        this.mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
                .andExpect(jsonPath("$.errors.password").value("Password must be at least 6 characters"))
                .andExpect(jsonPath("$.errors.email").value("Invalid Email format"))
                .andExpect(jsonPath("$.path").value("/api/auth/login"));
    }

    @Test
    void register_withValidInput_returnRegisterResponse() throws Exception {
        RegisterRequest body = new RegisterRequest("test@gmail.com", "testabc");
        String json = objectMapper.writeValueAsString(body);

        RegisterResponse registerResponse = new RegisterResponse(body.email(), Roles.USER.name());

        given(this.authService.register(any(RegisterRequest.class)))
                .willReturn(registerResponse);
        //when and then
        this.mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Register User success"))
                .andExpect(jsonPath("$.data.email").value("test@gmail.com"))
                .andExpect(jsonPath("$.data.role").value(Roles.USER.name()));

    }

    @Test
    void register_withDuplicateEmail_throwsAlreadyResourceException() throws Exception {
        RegisterRequest body = new RegisterRequest("test@gmail.com", "testabc");
        String json = objectMapper.writeValueAsString(body);

        given(this.authService.register(any(RegisterRequest.class)))
                .willThrow(new AlreadyResourceException("Email already exists in system"));
        //when and then
        this.mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Resource already exists"))
                .andExpect(jsonPath("$.errors").value("Email already exists in system"))
                .andExpect(jsonPath("$.path").value("/api/auth/register"));

    }

    @Test
    void register_withInValidInput_throwsBadRequestException() throws Exception {
        LoginRequest body = new LoginRequest("testgmail.com", "test");
        String json = objectMapper.writeValueAsString(body);


        given(this.authService.login(any(LoginRequest.class)))
                .willThrow(DataIntegrityViolationException.class);

        //when and then
        this.mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
                .andExpect(jsonPath("$.errors.password").value("Password must be at least 6 characters"))
                .andExpect(jsonPath("$.errors.email").value("Invalid Email format"))
                .andExpect(jsonPath("$.path").value("/api/auth/login"));
    }

    @Test
    void refresh_withValidToken_returnsRefreshTokenResponse() throws Exception {
        RefreshTokenRequest body = new RefreshTokenRequest("valid-token");
        String json = objectMapper.writeValueAsString(body);

        RefreshTokenResponse response = new RefreshTokenResponse("new-token", "valid-token");


        given(this.authService.refresh(any(RefreshTokenRequest.class)))
                .willReturn(response);

        //when and then
        this.mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Refresh new token success"))
                .andExpect(jsonPath("$.data.newToken").value("new-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("valid-token"));

    }


    @Test
    void refresh_withExpiryToken_returnsUnauthorizedException() throws Exception {
        RefreshTokenRequest body = new RefreshTokenRequest("invalid-token");
        String json = objectMapper.writeValueAsString(body);

        given(this.authService.refresh(any(RefreshTokenRequest.class)))
                .willThrow(new InvalidRefreshTokenException("Refresh token expired"));

        //when and then
        this.mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Unauthorized"))
                .andExpect(jsonPath("$.errors[0]").value("Refresh token expired"))
                .andExpect(jsonPath("$.path").value("/api/auth/refresh"));

    }
}