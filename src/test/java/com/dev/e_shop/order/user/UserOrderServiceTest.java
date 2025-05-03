package com.dev.e_shop.order.user;

import com.dev.e_shop.cart.UserCartRepository;
import com.dev.e_shop.cart.dto.CartDto;
import com.dev.e_shop.dto.PaginationResponse;
import com.dev.e_shop.exception.CartItemNotFoundException;
import com.dev.e_shop.order.Order;
import com.dev.e_shop.order.OrderRepository;
import com.dev.e_shop.order.dto.OrderRequest;
import com.dev.e_shop.order.dto.OrderResponse;
import com.dev.e_shop.order.item.OrderItem;
import com.dev.e_shop.order.item.OrderItemRepository;
import com.dev.e_shop.order.item.OrderItemService;
import com.dev.e_shop.order.mapper.OrderMapper;
import com.dev.e_shop.order.status.OrderStatus;
import com.dev.e_shop.product.Product;
import com.dev.e_shop.product.ProductRepository;
import com.dev.e_shop.user.User;
import com.dev.e_shop.user.UserDetail;
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
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;

@ExtendWith(MockitoExtension.class)
class UserOrderServiceTest {
    @InjectMocks
    UserOrderService orderService;

    @Mock
    OrderRepository orderRepository;

    @Mock
    OrderItemRepository orderItemRepository;

    @Mock
    UserCartRepository userCartRepository;

    @Mock
    ProductRepository productRepository;

    @Mock
    OrderItemService orderItemService;

    @Mock
    OrderMapper orderMapper;

    private UserDetail userDetail;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setId(1L);

        userDetail = new UserDetail(user);
    }

    @Test
    void getOrders_WithPagination_Limit2_ReturnsOrderResponses() {
        //given
        int page = 0;
        int size = 2;

        Order order1 = new Order();
        order1.setId(1L);
        order1.setUserId(1L);

        Order order2 = new Order();
        order2.setId(2L);
        order2.setUserId(1L);

        Order order3 = new Order();
        order3.setId(3L);
        order3.setUserId(2L);

        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);

        int start = page * size;
        int end = Math.min(start + size, orders.size());
        List<Order> pagedList = orders.subList(start, end);
        Page<Order> orderPage = new PageImpl<>(pagedList, PageRequest.of(page, size), orders.size());

        given(this.orderRepository.findAllByUserId(any(Pageable.class), eq(1L)))
                .willReturn(orderPage);

        OrderResponse response = new OrderResponse(1L,
                OrderStatus.PENDING.name(),
                BigDecimal.valueOf(5000),
                LocalDateTime.parse("2025-05-01T10:00:00"));
        //when

        Map<String, Object> actual = this.orderService.getOrderByPagination(userDetail, page, size);

        //then
        assertThat(actual).isNotNull();
        assertThat(actual).containsKeys("orders", "pagination");
        assertThat(actual.get("orders")).isInstanceOf(List.class);
        assertThat(((List<?>) actual.get("orders")).size()).isEqualTo(size);

        PaginationResponse pagination = (PaginationResponse) actual.get("pagination");
        assertThat(pagination.currentPage()).isEqualTo(page);
        assertThat(pagination.totalPage()).isEqualTo(1);
        assertThat(pagination.totalItems()).isEqualTo(orders.size());
        assertThat(pagination.pageSize()).isEqualTo(size);
    }

    @Test
    void create_Success_ReturnOrderResponses() {
        //given
        OrderItem item1 = new OrderItem();
        item1.setId(1L);
        item1.setOrderId(1L);
        item1.setPrice(new BigDecimal(500));
        item1.setProductId(1L);
        item1.setProductName("apple");
        item1.setQuantity(1);

        OrderItem item2 = new OrderItem();
        item2.setId(2L);
        item2.setOrderId(1L);
        item2.setPrice(new BigDecimal(500));
        item2.setProductId(2L);
        item2.setProductName("banana");
        item2.setQuantity(1);

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(item1);
        orderItems.add(item2);

        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setTotalAmount(BigDecimal.valueOf(1000));
        order.setCreateAt(LocalDateTime.parse("2025-05-01T10:00:00"));
        order.setUpdatedAT(LocalDateTime.parse("2025-05-01T10:00:00"));

        Product product = new Product();
        product.setId(1L);
        product.setName("apple");
        product.setPrice(BigDecimal.valueOf(500));

        CartDto cartDto1 = new CartDto(1,1, 1);
        CartDto cartDto2 = new CartDto(2,2, 2);

        List<CartDto> cartItems = new ArrayList<>();
        cartItems.add(cartDto1);
        cartItems.add(cartDto2);

        OrderRequest orderRequest = new OrderRequest(cartItems);
        OrderResponse response = new OrderResponse(1L,
                OrderStatus.PENDING.name(),
                BigDecimal.valueOf(5000),
                LocalDateTime.parse("2025-05-01T10:00:00"));

        given(this.orderRepository.save(any(Order.class)))
                .willReturn(order);
        given(this.userCartRepository.checkAllIdsExist(List.of(1L, 2L), 2, userDetail.getId()))
                .willReturn(true);

        given(this.productRepository.findById(1L)).willReturn(Optional.of(product));
        given(this.productRepository.findById(2L)).willReturn(Optional.of(product));
        willDoNothing().given(this.userCartRepository).deleteById(1L);
        willDoNothing().given(this.userCartRepository).deleteById(2L);
        given(this.orderMapper.toOrderResponse(any(Order.class)))
                .willReturn(response);
        //when
        OrderResponse actual = this.orderService.create(userDetail, orderRequest);
        //then
        assertThat(actual).isNotNull();
        assertThat(actual.id()).isEqualTo(1L);
        assertThat(actual.totalAmount()).isEqualTo(BigDecimal.valueOf(5000));
        assertThat(actual.createAt()).isEqualTo("2025-05-01T10:00:00");
    }

    @Test
    void create_WithInvalidCartItemId_ThrowsCartItemNotFoundException() {
        //given
        OrderItem item1 = new OrderItem();
        item1.setId(1L);
        item1.setOrderId(1L);
        item1.setPrice(new BigDecimal(500));
        item1.setProductId(1L);
        item1.setProductName("apple");
        item1.setQuantity(1);

        OrderItem item2 = new OrderItem();
        item2.setId(2L);
        item2.setOrderId(1L);
        item2.setPrice(new BigDecimal(500));
        item2.setProductId(2L);
        item2.setProductName("banana");
        item2.setQuantity(1);

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(item1);
        orderItems.add(item2);

        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setTotalAmount(BigDecimal.valueOf(1000));
        order.setCreateAt(LocalDateTime.parse("2025-05-01T10:00:00"));
        order.setUpdatedAT(LocalDateTime.parse("2025-05-01T10:00:00"));

        CartDto cartDto1 = new CartDto(1L, 1L, 1);
        CartDto cartDto2 = new CartDto(2L, 2L, 1);

        List<CartDto> cartItems = new ArrayList<>();
        cartItems.add(cartDto1);
        cartItems.add(cartDto2);

        OrderRequest orderRequest = new OrderRequest(cartItems);

        given(this.orderRepository.save(any(Order.class)))
                .willReturn(order);
        given(this.userCartRepository.checkAllIdsExist(List.of(1L, 2L), 2, userDetail.getId()))
                .willReturn(false);
        //when
        assertThrows(CartItemNotFoundException.class, () -> {
            OrderResponse actual = this.orderService.create(userDetail, orderRequest);
        });

    }

    @Test
    void getOrderDetail_withLimit2_returns2Items() {
        //given
        int page = 0;
        int size =2;

        List<OrderItem> orderItems = List.of(new OrderItem(), new OrderItem());

        Map<String, Object> data = new HashMap<>();
        data.put("Items", orderItems);
        data.put("Pagination", "pagination-info");
        given(this.orderItemService.getItemsByOrderId(1L, page, size))
                .willReturn(data);
        //when
        Map<String, Object> actual = this.orderService.getOrderDetail(1L, page, size);
        //then
        assertThat(actual).isNotNull();
        assertThat(actual.get("Items")).isNotNull();
        assertThat((List<?>) actual.get("Items")).hasSize(2);
    }
}