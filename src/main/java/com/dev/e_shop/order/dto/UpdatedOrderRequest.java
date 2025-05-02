package com.dev.e_shop.order.dto;

import jakarta.validation.constraints.NotNull;

public record UpdatedOrderRequest(
        @NotNull(message = "OderId is required")
        long orderId
) {
}
