package com.dev.e_shop.product;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "isDeleted"})
})
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("is_deleted = false")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "stock", nullable = false)
    @Min(0)
    private int stock;

    @Column(name = "category_id", nullable = false)
    private int categoryId;

    @Column(name = "brand", nullable = false)
    private String brand;

    @Column(name = "img_url")
    private String imgUrl;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAT;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}

