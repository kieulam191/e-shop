package com.dev.e_shop.order;

import com.dev.e_shop.order.status.Orders;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private long userId;

    @Enumerated(value = EnumType.STRING)
    private Orders status = Orders.PENDING;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAT;
}
