package com.dev.e_shop.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Collections;

public record RegisterResponse(
        @NotBlank (message = "Email must not be empty")
        @Email (message = "Invalid Email format")
        String email,

        String role) { }
