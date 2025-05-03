package com.dev.e_shop.order.item.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        long productId,
        String productName,
        int quantity,
        BigDecimal price
) {
}
