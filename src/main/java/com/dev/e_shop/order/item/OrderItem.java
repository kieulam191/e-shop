package com.dev.e_shop.order.item;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "orderItem")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long productId;

    @Column
    private String productName;

    @Column
    private BigDecimal price;

    @Column(nullable = false)
    private int quantity;
}
