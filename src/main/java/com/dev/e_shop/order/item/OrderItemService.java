package com.dev.e_shop.order.item;

import com.dev.e_shop.dto.PaginationResponse;
import com.dev.e_shop.exception.custom.NotFoundException;
import com.dev.e_shop.order.item.mapper.OrderItemMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderItemService {
    private final String ORDER_ITEM_KEY = "Items";
    private final String PAGINATION_KEY = "Pagination";

    private final OrderItemRepository orderItemRepository;
    private final OrderItemMapper orderItemMapper;

    public OrderItemService(OrderItemRepository orderItemRepository, OrderItemMapper orderItemMapper) {
        this.orderItemRepository = orderItemRepository;
        this.orderItemMapper = orderItemMapper;
    }

    public Map<String, Object> getItemsByOrderId(long orderId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderItem> orderItemPages = orderItemRepository.findAllByOrderId(pageable, orderId);

        if(orderItemPages.getContent().isEmpty()) {
            throw new NotFoundException("Order not found");
        }

        return createDataByPagination(orderItemPages);
    }

    private Map<String, Object> createDataByPagination(Page<OrderItem> orderPage) {
        Map<String, Object> data = new HashMap<>();
        data.put(ORDER_ITEM_KEY, orderPage.getContent()
                .stream()
                .map(orderItemMapper::toOrderItemResponse)
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
