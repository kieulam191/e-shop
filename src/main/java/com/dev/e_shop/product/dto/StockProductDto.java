package com.dev.e_shop.product.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StockProductDto {
    @Min(value = 1, message = "Stock must be greater than 0")
    private int stock;
}
