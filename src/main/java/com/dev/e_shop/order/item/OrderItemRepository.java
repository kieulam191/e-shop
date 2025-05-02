package com.dev.e_shop.order.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    Page<OrderItem> findAllByOrderId(Pageable pageable, long orderId);
}
