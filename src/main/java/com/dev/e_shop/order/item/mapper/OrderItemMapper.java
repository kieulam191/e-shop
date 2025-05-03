package com.dev.e_shop.order.item.mapper;

import com.dev.e_shop.order.item.OrderItem;
import com.dev.e_shop.order.item.dto.OrderItemResponse;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface OrderItemMapper {
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);
}
