package com.dev.e_shop.order.dto;

import com.dev.e_shop.order.status.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdatedOrderRequest(
        @NotNull(message = "orderId is required")
        long orderId,

        @NotNull(message = "status is required")
        OrderStatus status
) { }
