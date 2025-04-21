package com.dev.e_shop.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductRequest {
    @NotBlank(message = "name cannot be empty")
    private String name;

    @NotNull(message = "price is required")
    @DecimalMin(value = "100", message = "Price must be greater than 100")
    private BigDecimal price;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    @NotNull(message = "Category ID is required")
    private int categoryId;

    @NotBlank
    @Size(min = 1, message = "Brand cannot be empty")
    private String brand;

    @NotBlank
    private String imgUrl;
}

