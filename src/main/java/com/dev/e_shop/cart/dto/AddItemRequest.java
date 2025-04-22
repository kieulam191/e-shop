package com.dev.e_shop.cart.dto;

import jakarta.validation.constraints.NotNull;

public record AddItemRequest(
        @NotNull long userId,
        @NotNull long productId
) { }
