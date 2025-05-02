package com.dev.e_shop.order.user;

import com.dev.e_shop.cart.dto.CartDto;
import com.dev.e_shop.exception.CartItemNotFoundException;
import com.dev.e_shop.order.dto.OrderRequest;
import com.dev.e_shop.order.dto.OrderResponse;
import com.dev.e_shop.order.item.dto.OrderItemResponse;
import com.dev.e_shop.order.status.Orders;
import com.dev.e_shop.user.User;
import com.dev.e_shop.user.UserDetail;
import com.dev.e_shop.user.profile.Profile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @MockitoBean
    UserOrderService orderService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private UserDetail userDetail;

    @BeforeEach
    void setUp() {
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUserId(1L);

        User user = new User();
        user.setEmail("test@gmail.com");
        user.setId(1L);

        userDetail = new UserDetail(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetail, null,
                userDetail.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }


    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void create_WithExistingCartItem_ReturnsOrderResponse() throws Exception {
        //given
        OrderRequest orderRequest = new OrderRequest(List.of(
                new CartDto(1L, 1L, 1),
                new CartDto(2L, 2L, 1)
        ));

        String json = objectMapper.writeValueAsString(orderRequest);

        OrderResponse orderResponse = new OrderResponse(
                1L,
                Orders.PENDING.name(),
                BigDecimal.valueOf(5000),
                LocalDateTime.parse("2025-05-01T10:00:00"));

        given(this.orderService.create(any(UserDetail.class), any(OrderRequest.class)))
                .willReturn(orderResponse);


        //when and then
        this.mockMvc.perform(post("/api/user/orders/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Create a order success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value(Orders.PENDING.name()))
                .andExpect(jsonPath("$.data.totalAmount").value(5000))
                .andExpect(jsonPath("$.data.createAt").value("2025-05-01T10:00:00"));
    }

    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void create_withInvalidCartItemIds_throwsCartItemNotFoundException() throws Exception {
        //given
        OrderRequest orderRequest = new OrderRequest(List.of(
                new CartDto(1L, 1L, 1),
                new CartDto(2L, 2L, 1)
        ));

        String json = objectMapper.writeValueAsString(orderRequest);

        given(this.orderService.create(any(UserDetail.class), any(OrderRequest.class)))
                .willThrow(new CartItemNotFoundException("Cart Item not found"));

        //when and then
        this.mockMvc.perform(post("/api/user/orders/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.errors[0]").value("Cart Item not found"))
                .andExpect(jsonPath("$.path").value("/api/user/orders/"));
    }

    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void getOrdersByPagination_WithLimit2_ReturnsOrderResponses() throws Exception {
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
                        Orders.PENDING.name(),
                        BigDecimal.valueOf(5000),
                        LocalDateTime.parse("2025-05-01T10:00:00")
                ),
                new OrderResponse(
                        2L,
                        Orders.PENDING.name(),
                        BigDecimal.valueOf(5000),
                        LocalDateTime.parse("2025-05-01T10:00:00")
                )
        );

        Map<String, Object> data = new HashMap<>();
        data.put("orders", orderResponses);
        data.put("pagination", infoPage);

        given(this.orderService.getOrderByPagination(userDetail,page, size))
                .willReturn(data);

        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/user/orders/")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get orders success"))
                .andExpect(jsonPath("$.data", Matchers.hasKey("orders")))
                .andExpect(jsonPath("$.data", Matchers.hasKey("pagination")))
                .andExpect(jsonPath("$.data.orders", Matchers.hasSize(2)));
    }

    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void getOrderDetail_Success_ReturnsOrderResponse()throws Exception {
        //given
        int page = 0, size = 1;

        Map<String, Number> infoPage = new HashMap<>();
        infoPage.put("currentPage", 0);
        infoPage.put("totalPage", 0);
        infoPage.put("totalItems", 2);
        infoPage.put("pageSize", 1);

        List<OrderItemResponse> orderResponses = List.of(
                new OrderItemResponse(
                        1L,
                        "apple",
                       1,
                        BigDecimal.valueOf(5000)
                ),
                new OrderItemResponse(
                        2L,
                        "apple",
                        1,
                        BigDecimal.valueOf(5000)
                )
        );

        Map<String, Object> data = new HashMap<>();
        data.put("orderItems", orderResponses);
        data.put("pagination", infoPage);

        given(this.orderService.getOrderDetail(1L,page, size))
                .willReturn(data);

        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/user/orders/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get orders success"))
                .andExpect(jsonPath("$.data", Matchers.hasKey("orderItems")))
                .andExpect(jsonPath("$.data", Matchers.hasKey("pagination")))
                .andExpect(jsonPath("$.data.orderItems", Matchers.hasSize(2)));
    }
}