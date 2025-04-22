package com.dev.e_shop.cart.dto;

import java.math.BigDecimal;
import java.util.Set;

public record CartResponse(
        Set<CartDto> carts,
        BigDecimal totalPrice
) {
}
