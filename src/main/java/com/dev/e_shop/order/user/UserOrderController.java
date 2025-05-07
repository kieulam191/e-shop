package com.dev.e_shop.order.user;

import com.dev.e_shop.dto.ApiResponse;
import com.dev.e_shop.dto.PaginationDto;
import com.dev.e_shop.order.dto.OrderRequest;
import com.dev.e_shop.order.dto.OrderResponse;
import com.dev.e_shop.user.UserDetail;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user/orders")
public class UserOrderController {

    private final UserOrderService orderService;

    public UserOrderController(UserOrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse< Map<String, Object>>> getOrderByPagination(
            @AuthenticationPrincipal UserDetail userDetail,
            @Valid PaginationDto paginationDto
    ) {
        Map<String, Object> response = orderService.getOrderByPagination(userDetail, paginationDto.getPageInt(), paginationDto.getSizeInt());

        return ResponseEntity.status(200)
                .body(new ApiResponse<>(
                        200,
                        "Get orders success",
                        response
                ));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse< Map<String, Object>>> getOrderDetail(
            @PathVariable long orderId,
            @Valid PaginationDto paginationDto
    ) {
        Map<String, Object> response = orderService.getOrderDetail(
                orderId,
                paginationDto.getPageInt(),
                paginationDto.getSizeInt());

        return ResponseEntity.status(200)
                .body(new ApiResponse<>(
                        200,
                        "Get orders success",
                        response
                ));
    }

    @PostMapping("/")
    public ResponseEntity<ApiResponse<OrderResponse>> create(
            @AuthenticationPrincipal UserDetail userDetail,
            @Valid @RequestBody OrderRequest body
    ) {
        OrderResponse response = orderService.create(userDetail, body);

        return ResponseEntity.status(201)
                .body(new ApiResponse<>(
                        201,
                        "Create a order success",
                        response
                ));
    }
}
