package com.dev.e_shop.order.admin;

import com.dev.e_shop.exception.custom.NotFoundException;
import com.dev.e_shop.order.dto.OrderResponse;
import com.dev.e_shop.order.dto.UpdatedOrderRequest;
import com.dev.e_shop.order.status.OrderStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin@gamil.com", roles = {"ADMIN"})
class AdminOrderControllerTest {

    @MockitoBean
    AdminOrderService adminOrderService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    UpdatedOrderRequest updatedOrderRequest;

    @BeforeEach
    void setUp() {
        updatedOrderRequest = new UpdatedOrderRequest(1L, OrderStatus.SHIPPED);
    }

    @Test
    void updateOrderState_WithExistingOrderId_ReturnsUpdatedOrderResponse() throws Exception {
        OrderResponse response = new OrderResponse(1L,
                OrderStatus.SHIPPED.name(),
                BigDecimal.valueOf(5000),
                LocalDateTime.parse("2025-05-01T10:00:00"));

        String json = objectMapper.writeValueAsString(updatedOrderRequest);

        given(this.adminOrderService.updateOrderState(updatedOrderRequest))
                .willReturn(response);
        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.patch("/api/admin/orders/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Updated order status success"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.status").value(OrderStatus.SHIPPED.name()));
    }

    @Test
    void updateOrderState_WithNonExistentOrderId_ReturnsNotFoundException() throws Exception {
        String json = objectMapper.writeValueAsString(updatedOrderRequest);

        given(this.adminOrderService.updateOrderState(updatedOrderRequest))
                .willThrow(new NotFoundException("Order Id not found"));
        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.patch("/api/admin/orders/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Resource not found"))
                .andExpect(jsonPath("$.errors[0]").value("Order Id not found"))
                .andExpect(jsonPath("$.path").value("/api/admin/orders/"));
    }

    @Test
    void getOrdersByStatus_WithPendingStatus_ReturnsLimitedOrders() throws Exception {
        //given
        int page = 0, size = 1;

        Map<String, Number> infoPage = new HashMap<>();
        infoPage.put("currentPage", 0);
        infoPage.put("totalPage", 0);
        infoPage.put("totalItems", 2);
        infoPage.put("pageSize", 1);

        List<OrderResponse> orderResponses = List.of(
                new OrderResponse(
                        1L,
                        OrderStatus.PENDING.name(),
                        BigDecimal.valueOf(5000),
                        LocalDateTime.parse("2025-05-01T10:00:00")
                ),
                new OrderResponse(
                        2L,
                        OrderStatus.PENDING.name(),
                        BigDecimal.valueOf(5000),
                        LocalDateTime.parse("2025-05-01T10:00:00")
                )
        );

        Map<String, Object> data = new HashMap<>();
        data.put("orders", orderResponses);
        data.put("pagination", infoPage);

        given(this.adminOrderService.getAllOrderByStatus(page, size, OrderStatus.PENDING))
                .willReturn(data);

        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/orders/")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get orders success"))
                .andExpect(jsonPath("$.data", Matchers.hasKey("orders")))
                .andExpect(jsonPath("$.data", Matchers.hasKey("pagination")))
                .andExpect(jsonPath("$.data.orders", Matchers.hasSize(2)));
    }
}