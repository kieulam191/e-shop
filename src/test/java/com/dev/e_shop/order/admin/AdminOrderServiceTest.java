package com.dev.e_shop.order.admin;

import com.dev.e_shop.exception.NotFoundException;
import com.dev.e_shop.order.Order;
import com.dev.e_shop.order.OrderRepository;
import com.dev.e_shop.order.dto.OrderResponse;
import com.dev.e_shop.order.dto.UpdatedOrderRequest;
import com.dev.e_shop.order.mapper.OrderMapper;
import com.dev.e_shop.order.status.Orders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AdminOrderServiceTest {

    @InjectMocks
    AdminOrderService adminOrderService;

    @Mock
    OrderRepository orderRepository;

    @Mock
    OrderMapper orderMapper;

    UpdatedOrderRequest updatedOrderRequest;

    @BeforeEach
    void setUp() {
        updatedOrderRequest = new UpdatedOrderRequest(1L);
    }

    @Test
    void updateOrderState_withExistingOrderId_returnsUpdatedOrderResponse() {
        //given
        Order order = new Order();
        order.setStatus(Orders.PENDING);

        Order updatedOrderState = new Order();
        order.setStatus(Orders.SHIPPED);

        OrderResponse response = new OrderResponse(1L,
                Orders.SHIPPED.name(),
                BigDecimal.valueOf(5000),
                LocalDateTime.parse("2025-05-01T10:00:00"));


        given(orderRepository.findById(1L)).willReturn(Optional.of(order));
        given(orderRepository.save(any(Order.class))).willReturn(updatedOrderState);
        given(orderMapper.toOrderResponse(updatedOrderState)).willReturn(response);
        //when

        OrderResponse actual = this.adminOrderService.updateOrderState(updatedOrderRequest);

        //that
        assertThat(actual).isNotNull();
        assertThat(actual.id()).isEqualTo(1L);
        assertThat(actual.status()).isEqualTo(Orders.SHIPPED.name());
    }

    @Test
    void updateOrderState_withNonOrderId_throwsNotFoundException() {
        //given
        Order order = new Order();
        order.setStatus(Orders.PENDING);

        given(orderRepository.findById(1L)).willReturn(Optional.empty());
        //when

        assertThrows(NotFoundException.class, () -> {
            OrderResponse actual = this.adminOrderService.updateOrderState(updatedOrderRequest);
        });
    }

    @Test
    void getAllOrderByStatus_withPendingStatus_returnsLimit2() {
        //given
        int page = 0;
        int size = 2;

        Order order1 = new Order();
        order1.setId(1L);
        order1.setStatus(Orders.PENDING);
        order1.setUserId(1L);

        Order order2 = new Order();
        order2.setId(2L);
        order2.setStatus(Orders.PENDING);
        order2.setUserId(1L);


        List<Order> orders = List.of(order1, order2);

        int start = page * size;
        int end = Math.min(start + size, orders.size());
        List<Order> pagedList = orders.subList(start, end);
        Page<Order> orderPage = new PageImpl<>(pagedList, PageRequest.of(page, size), orders.size());

        given(this.orderRepository.findAllByStatus(any(Pageable.class), eq(Orders.PENDING)))
                .willReturn(orderPage);
        //when
        Map<String, Object> actual = this.adminOrderService.getAllOrderByStatus(page, size, Orders.PENDING);

        //then
        assertThat(actual).isNotNull();
        assertThat((List<?>) actual.get("orders")).hasSize(2);
    }
}