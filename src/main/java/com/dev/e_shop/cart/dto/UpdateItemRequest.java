package com.dev.e_shop.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateItemRequest(
        @NotNull long cartItemId,
        @NotNull @Min(value = 1, message = "Amount must be greater than 0")
        int amount
) {
}
