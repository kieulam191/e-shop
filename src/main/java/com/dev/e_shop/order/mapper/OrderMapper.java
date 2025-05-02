package com.dev.e_shop.order.mapper;

import com.dev.e_shop.order.Order;
import com.dev.e_shop.order.dto.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
@Component
public interface OrderMapper {

    @Mapping(source = "createAt", target = "createAt", qualifiedByName = "convertLocalDateTime")
    OrderResponse toOrderResponse(Order order);

    @Named("convertLocalDateTime")
    static LocalDateTime convertLocalDateTime(LocalDateTime time) {
        return time == null ? null : time.withNano(0);
    }
}
