package com.dev.e_shop.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CartDto {
    private long id;
    private long productId;
    private int quantity;
}
