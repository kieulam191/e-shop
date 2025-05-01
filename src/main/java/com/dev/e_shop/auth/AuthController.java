package com.dev.e_shop.auth;

import com.dev.e_shop.auth.refreshToken.dto.RefreshTokenRequest;
import com.dev.e_shop.auth.refreshToken.dto.RefreshTokenResponse;
import com.dev.e_shop.dto.ApiResponse;
import com.dev.e_shop.user.dto.RegisterRequest;
import com.dev.e_shop.user.dto.LoginRequest;
import com.dev.e_shop.user.dto.LoginResponse;
import com.dev.e_shop.user.dto.RegisterResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest body) {

        LoginResponse savedUser = authService.login(body);

        return ResponseEntity.status(200)
                .body(new ApiResponse<LoginResponse>(
                        200,
                        "Login success",
                        savedUser
                ));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest body) {

        RegisterResponse savedUser = authService.register(body);

        return ResponseEntity.status(201)
                .body(new ApiResponse<RegisterResponse>(
                        201,
                        "Register User success",
                        savedUser
                ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest body) {
        RefreshTokenResponse refresh = authService.refresh(body);

        return ResponseEntity.status(200)
                .body(new ApiResponse<RefreshTokenResponse>(
                        200,
                        "Refresh new token success",
                        refresh
                ));
    }
}
