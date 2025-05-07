package com.dev.e_shop.order.user;

import com.dev.e_shop.cart.UserCartRepository;
import com.dev.e_shop.dto.PaginationResponse;
import com.dev.e_shop.exception.custom.CartItemNotFoundException;
import com.dev.e_shop.exception.custom.NotFoundException;
import com.dev.e_shop.order.Order;
import com.dev.e_shop.order.OrderRepository;
import com.dev.e_shop.order.dto.OrderRequest;
import com.dev.e_shop.order.dto.OrderResponse;
import com.dev.e_shop.order.item.OrderItem;
import com.dev.e_shop.order.item.OrderItemRepository;
import com.dev.e_shop.order.item.OrderItemService;
import com.dev.e_shop.order.mapper.OrderMapper;
import com.dev.e_shop.product.Product;
import com.dev.e_shop.product.ProductRepository;
import com.dev.e_shop.user.UserDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserOrderService {
    private final String ORDER_KEY = "orders";
    private final String PAGINATION_KEY = "pagination";

    private final OrderItemService orderItemService;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserCartRepository userCartRepository;
    private final ProductRepository productRepository;

    private final OrderMapper orderMapper;

    public UserOrderService(OrderItemService orderItemService, OrderRepository orderRepository, OrderItemRepository orderItemRepository, UserCartRepository userCartRepository, ProductRepository productRepository, OrderMapper orderMapper) {
        this.orderItemService = orderItemService;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userCartRepository = userCartRepository;
        this.productRepository = productRepository;
        this.orderMapper = orderMapper;
    }

    public Map<String, Object> getOrderByPagination(UserDetail userDetail, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage = this.orderRepository.findAllByUserId(pageable, userDetail.getId());

        return createDataByPagination(orderPage);
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponse create(UserDetail userDetail, OrderRequest body) {
        Order savedOrder = createOrder(userDetail.getId());
        createOrderItem(body, savedOrder.getId(), userDetail.getId());
        
        return orderMapper.toOrderResponse(savedOrder);
    }


    public Map<String, Object> getOrderDetail(long orderId, int page, int size) {
        return orderItemService.getItemsByOrderId(orderId, page, size);
    }

    private Order createOrder(long userId) {
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(getTotalPrice(userId));

        return orderRepository.save(order);
    }

    private BigDecimal getTotalPrice(long userId) {
        BigDecimal totalPrice = this.userCartRepository.calculateTotalPriceByUserId(userId);
        return totalPrice != null ? totalPrice : BigDecimal.ZERO;
    }
    
    private void createOrderItem(OrderRequest body, long orderId, long userId) {

        boolean allIdsExist = checkValidCartItem(body, userId);

        if(allIdsExist) {
            body.orderItems().stream()
                    .map(cartItem -> {
                        Product product = productRepository.findById(cartItem.getProductId())
                                .orElseThrow(() -> new NotFoundException("Product not fount"));

                        OrderItem orderItem = new OrderItem();
                        orderItem.setProductId(cartItem.getProductId());
                        orderItem.setOrderId(orderId);
                        orderItem.setPrice(product.getPrice());
                        orderItem.setProductName(product.getName());
                        orderItem.setQuantity(cartItem.getQuantity());

                        OrderItem save = this.orderItemRepository.save(orderItem);

                        userCartRepository.deleteById(cartItem.getId());
                        return save;
                    })
                    .collect(Collectors.toList());
        } else {
            throw new CartItemNotFoundException("Cart Item not found");
        }
    }

    private boolean checkValidCartItem(OrderRequest body, long userId) {
        List<Long> ids = body.orderItems().stream()
                .map(cartDto -> cartDto.getId())
                .collect(Collectors.toList());

        return userCartRepository.checkAllIdsExist(ids, ids.size(), userId);
    }

    private Map<String, Object> createDataByPagination(Page<Order> orderPage) {
        Map<String, Object> data = new HashMap<>();
        data.put(ORDER_KEY, orderPage.getContent()
                .stream()
                .map(order -> {
                    return orderMapper.toOrderResponse(order);
                })
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
