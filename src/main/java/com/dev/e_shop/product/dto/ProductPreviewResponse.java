package com.dev.e_shop.product.dto;

import java.math.BigDecimal;

public record ProductPreviewResponse(
        long id,
        String name,
        BigDecimal price
) {
}
