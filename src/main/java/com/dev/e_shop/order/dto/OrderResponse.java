package com.dev.e_shop.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(
        long id,
        String status,
        BigDecimal totalAmount,
        LocalDateTime createAt
) { }
