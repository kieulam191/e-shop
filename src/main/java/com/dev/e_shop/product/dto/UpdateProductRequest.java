package com.dev.e_shop.product.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
public class UpdateProductRequest {
    @Pattern(regexp = "^(?!\\s*$)[\\p{L}\\p{N} .,'\\-()]{1,255}$", message = "Name must to has at least one and do not contain special characters")
    private String name;

    @DecimalMin(value = "0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    @Min(0)
    private int categoryId;

    @Size(min = 1, message = "Brand cannot be empty")
    private String brand;

    @Size(max = 100, message = "unknow")
    private String imgUrl;
}
