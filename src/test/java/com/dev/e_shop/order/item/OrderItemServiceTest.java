package com.dev.e_shop.order.item;

import com.dev.e_shop.exception.NotFoundException;
import com.dev.e_shop.order.item.dto.OrderItemResponse;
import com.dev.e_shop.order.item.mapper.OrderItemMapper;
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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

    @InjectMocks
    OrderItemService orderItemService;

    @Mock
    OrderItemRepository orderItemRepository;

    @Mock
    OrderItemMapper orderItemMapper;

    @Test
    void getItemsByOrderId_WithValidOrderId_ReturnLimit2Items() {
        //given
        int page = 0;
        int size = 2;

        OrderItem item1 = new OrderItem();
        item1.setId(1L);

        OrderItem item2 = new OrderItem();
        item2.setId(1L);


        List<OrderItem> orders = List.of(item1, item2);

        int start = page * size;
        int end = Math.min(start + size, orders.size());
        List<OrderItem> pagedList = orders.subList(start, end);
        Page<OrderItem> orderPage = new PageImpl<>(pagedList, PageRequest.of(page, size), orders.size());

        given(this.orderItemRepository.findAllByOrderId(any(Pageable.class), eq(1L)))
                .willReturn(orderPage);
        given(this.orderItemMapper.toOrderItemResponse(any(OrderItem.class)))
                .willReturn(
                        new OrderItemResponse(1L, "APPLE", 1, BigDecimal.valueOf(500)),
                        new OrderItemResponse(2L, "APPLE", 1, BigDecimal.valueOf(500))
                );
        //when
        Map<String, Object> actual = this.orderItemService.getItemsByOrderId(1L, page, size);

        //then
        assertThat(actual).isNotNull();
        assertThat((List<?>) actual.get("Items")).hasSize(2);
    }

    @Test
    void getItemsByOrderId_WithInvalidOrderId_ThrowsNotFoundException() {
        //given
        int page = 0;
        int size = 2;

        List<OrderItem> orders = List.of();

        int start = page * size;
        int end = Math.min(start + size, orders.size());
        List<OrderItem> pagedList = orders.subList(start, end);
        Page<OrderItem> orderPage = new PageImpl<>(pagedList, PageRequest.of(page, size), orders.size());

        given(this.orderItemRepository.findAllByOrderId(any(Pageable.class), eq(1L)))
                .willReturn(orderPage);
        //when
        assertThrows(NotFoundException.class, () -> {
            Map<String, Object> actual = this.orderItemService.getItemsByOrderId(1L, page, size);
        });
        //then
    }

}