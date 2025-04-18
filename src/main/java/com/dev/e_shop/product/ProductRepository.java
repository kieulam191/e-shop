package com.dev.e_shop.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p.id AS id, p.name AS name, p.stock AS stock FROM Product p WHERE p.id = :id")
    Optional<StockView> findStockViewById(@Param("id") Long id);

    Optional<Product> findByNameIgnoreCase(String name);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    interface StockView {
        Long getId();
        String getName();
        int getStock();
    }
}
