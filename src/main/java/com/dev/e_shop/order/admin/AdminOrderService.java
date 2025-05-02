package com.dev.e_shop.order.admin;

import com.dev.e_shop.dto.PaginationResponse;
import com.dev.e_shop.exception.NotFoundException;
import com.dev.e_shop.order.Order;
import com.dev.e_shop.order.OrderRepository;
import com.dev.e_shop.order.dto.OrderResponse;
import com.dev.e_shop.order.dto.UpdatedOrderRequest;
import com.dev.e_shop.order.mapper.OrderMapper;
import com.dev.e_shop.order.status.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminOrderService {
    private final String ORDER_KEY = "orders";
    private final String PAGINATION_KEY = "pagination";

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public AdminOrderService(OrderRepository orderRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    public Map<String, Object> getAllOrderByStatus(int page, int size, Orders status) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPages = orderRepository.findAllByStatus(pageable, status);

        return createDataByPagination(orderPages);
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponse updateOrderState(UpdatedOrderRequest body) {
        return orderRepository.findById(body.orderId())
                .map(order -> {
                    order.setStatus(Orders.SHIPPED);

                    Order savedOrder = this.orderRepository.save(order);

                    return this.orderMapper.toOrderResponse(savedOrder);
                })
                .orElseThrow(() -> new NotFoundException("Order not found"));
    }

    private Map<String, Object> createDataByPagination(Page<Order> orderPage) {
        Map<String, Object> data = new HashMap<>();
        data.put(ORDER_KEY, orderPage.getContent()
                .stream()
                .map(order -> {
                    return orderMapper.toOrderResponse(order);
                })
                .collect(Collectors.toList())
        );
        data.put(PAGINATION_KEY, new PaginationResponse(
                orderPage.getNumber(),
                orderPage.getTotalPages(),
                orderPage.getTotalElements(),
                orderPage.getSize()
        ));

        return data;
    }
}
