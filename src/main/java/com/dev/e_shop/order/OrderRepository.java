package com.dev.e_shop.order;

import com.dev.e_shop.order.status.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findAllByUserId(Pageable orderPage, long userId);

    Page<Order> findAllByStatus(Pageable pageable, Orders orders);
}
