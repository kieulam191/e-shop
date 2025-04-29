package com.dev.e_shop.cart;

import com.dev.e_shop.cart.dto.AddItemRequest;
import com.dev.e_shop.cart.dto.CartDto;
import com.dev.e_shop.cart.dto.CartResponse;
import com.dev.e_shop.cart.dto.UpdateItemRequest;
import com.dev.e_shop.exception.NotFoundException;
import com.dev.e_shop.product.ProductRepository;
import com.dev.e_shop.user.UserDetail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

@Service
public class UserCartService {
    private final UserCartRepository userCartRepository;
    private final ProductRepository productRepository;

    public UserCartService(UserCartRepository userCartRepository, ProductRepository productRepository) {
        this.userCartRepository = userCartRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public void addCartItem(AddItemRequest body, UserDetail userDetail) {
        productRepository.findById(body.productId())
                .orElseThrow(() -> new NotFoundException("Product with ID " + body.productId() + " not found"));

        Optional<Cart> existingCartItem = userCartRepository
                .findByProductIdAndUserId(body.productId(), userDetail.getId());

        if (existingCartItem.isPresent()) {
            Cart item = existingCartItem.get();
            item.setQuantity(item.getQuantity() + 1);
            userCartRepository.save(item);
        } else {
            Cart newCartItem = Cart.builder()
                    .userId(userDetail.getId())
                    .productId(body.productId())
                    .quantity(1)
                    .build();
            userCartRepository.save(newCartItem);
        }
    }

    public CartResponse getCartByUserId(long userId) {
        Set<CartDto> items = this.userCartRepository
                .findItemsByUserid(userId);

        BigDecimal totalPrice = getTotalPrice(userId);

        return new CartResponse(items, totalPrice);
    }

    private BigDecimal getTotalPrice(long userId) {
        BigDecimal totalPrice = this.userCartRepository.calculateTotalPriceByUserId(userId);
        return totalPrice != null ? totalPrice : BigDecimal.ZERO;
    }

    @Transactional
    public void updateQuantityOfItem(UpdateItemRequest body, UserDetail userDetail) {
        userCartRepository.findByIdAndUserId(body.cartItemId(), userDetail.getId())
                .map(item -> {
                    item.setQuantity(body.amount());
                    return this.userCartRepository.save(item);
                })
                .orElseThrow(() -> createNotFoundException(body.cartItemId()));
    }

    @Transactional
    public void removeCartItem(long id, UserDetail userDetail) {
        this.userCartRepository.findByIdAndUserId(id, userDetail.getId())
                .orElseThrow(()-> createNotFoundException(id));

        this.userCartRepository.deleteById(id);
    }

    private NotFoundException createNotFoundException(long id) {
        return new NotFoundException(String.format("Item cart with ID %d not found in your cart", id));
    }
}
