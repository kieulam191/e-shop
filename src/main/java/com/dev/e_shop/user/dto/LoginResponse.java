package com.dev.e_shop.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collections;
import java.util.Set;

public record LoginResponse(
        @NotBlank
        @Email
        String email,

        String role,
        String token,
        String freshToken
) { }
