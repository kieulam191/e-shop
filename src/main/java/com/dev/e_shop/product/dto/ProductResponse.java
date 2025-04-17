package com.dev.e_shop.product.dto;

import java.math.BigDecimal;

public record ProductResponse(
         long id,
         String name,
         BigDecimal price,
         String description,
         long categoryId,
         String brand,
         String imgUrl) {
}
