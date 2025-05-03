package com.dev.e_shop.order.admin;

import com.dev.e_shop.dto.ApiResponse;
import com.dev.e_shop.order.dto.OrderResponse;
import com.dev.e_shop.order.dto.UpdatedOrderRequest;
import com.dev.e_shop.order.status.OrderStatus;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {
    private final AdminOrderService adminOrderService;

    public AdminOrderController(AdminOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOrderByStatus(
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "1", required = false) int size,
            @RequestParam(defaultValue = "PENDING", required = false) OrderStatus status
    ) {
        Map<String, Object> allOrder = adminOrderService.getAllOrderByStatus(page, size, status);

        return ResponseEntity.status(200)
                .body(new ApiResponse<>(
                        200,
                        "Get orders success",
                        allOrder
                ));
    }


    @PatchMapping("/")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderState(@Valid @RequestBody UpdatedOrderRequest body) {
        OrderResponse orderResponse = adminOrderService.updateOrderState(body);

        return ResponseEntity.status(200)
                .body(new ApiResponse<>(
                        200,
                        "Updated order status success",
                        orderResponse
                ));
    }
}
