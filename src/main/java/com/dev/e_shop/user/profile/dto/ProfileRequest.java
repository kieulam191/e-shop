package com.dev.e_shop.user.profile.dto;

import jakarta.validation.constraints.Size;

public record ProfileRequest(
        @Size(max = 255, message = "Address must be at most 255 characters")
        String address,
        @Size(min = 10, message = "Phone must be least at 10 number")
        String phone
) { }
