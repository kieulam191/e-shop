package com.dev.e_shop.order.dto;

import com.dev.e_shop.cart.dto.CartDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderRequest(
        @NotNull(message = "orderItem is required")
        @Size(min = 1, message = "Order must have at least one item")
        List<CartDto> orderItem
) { }
