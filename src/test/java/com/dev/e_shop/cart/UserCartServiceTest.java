package com.dev.e_shop.cart;

import com.dev.e_shop.cart.dto.AddItemRequest;
import com.dev.e_shop.cart.dto.CartDto;
import com.dev.e_shop.cart.dto.CartResponse;
import com.dev.e_shop.cart.dto.UpdateItemRequest;
import com.dev.e_shop.exception.NotFoundException;
import com.dev.e_shop.product.Product;
import com.dev.e_shop.product.ProductRepository;
import com.dev.e_shop.user.User;
import com.dev.e_shop.user.UserDetail;
import com.dev.e_shop.user.profile.Profile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCartServiceTest {
    @InjectMocks
    UserCartService userCartService;

    @Mock
    UserCartRepository userCartRespository;

    @Mock
    ProductRepository productRepository;

    UserDetail userDetail;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setId(1L);

        userDetail = new UserDetail(user);
    }


    @Test
    void addCartItem_withValidCartItem_savesCartItem() {
        // given
        AddItemRequest body = new AddItemRequest(1L);

        Cart cartItem = Cart.builder()
                .id(1L)
                .userId(1L)
                .productId(1L)
                .quantity(1)
                .build();
        given(this.productRepository.findById(1L)).willReturn(Optional.of(new Product()));
        given(userCartRespository.findByProductIdAndUserId(1L, 1L)).willReturn(Optional.empty());
        given(userCartRespository.save(any(Cart.class))).willReturn(cartItem);

        // when
        userCartService.addCartItem(body, userDetail);

        // then
        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(userCartRespository).save(cartCaptor.capture());

        Cart savedCart = cartCaptor.getValue();
        assertEquals(1L, savedCart.getUserId());
        assertEquals(1L, savedCart.getProductId());
        assertEquals(1, savedCart.getQuantity());

        verify(this.productRepository).findById(1L);
    }

    @Test
    void addCartItem_withExistingCartItem_increaseQuantityBy1() {
        // given
        AddItemRequest body = new AddItemRequest(1L);

        Cart existingCartItem = Cart.builder()
                .id(1L)
                .userId(1L)
                .productId(1L)
                .quantity(1)
                .build();

        Cart updatedCartItem = Cart.builder()
                .id(1L)
                .userId(1L)
                .productId(1L)
                .quantity(2) // updated
                .build();

        given(this.productRepository.findById(1L)).willReturn(Optional.of(new Product()));
        given(userCartRespository.findByProductIdAndUserId(1L, 1L))
                .willReturn(Optional.of(existingCartItem));

        given(userCartRespository.save(existingCartItem)).willReturn(updatedCartItem);

        // when
        userCartService.addCartItem(body, userDetail);

        // then
        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(userCartRespository).save(cartCaptor.capture());

        Cart savedCart = cartCaptor.getValue();
        assertEquals(1L, savedCart.getUserId());
        assertEquals(1L, savedCart.getProductId());
        assertEquals(2, savedCart.getQuantity());
    }

    @Test
    void addCartItem_withNotFoundProductId_throwsNotFoundException() {
        // given
        AddItemRequest body = new AddItemRequest(1000L);

        given(productRepository.findById(1000L))
                .willReturn(Optional.empty());

        // when
        assertThrows(NotFoundException.class, () -> {
            userCartService.addCartItem(body, userDetail);
        });

        // then
        verify(productRepository, times(1)).findById(1000L);
    }


    @Test
    void getCartByUserId_withExistingUserId_returnsCart() {
        //given
        Set<CartDto> carts = new HashSet<>();
        CartDto item1 = new CartDto(1,1,1);
        CartDto item2 = new CartDto(2,1,2);
        carts.add(item1);
        carts.add(item2);

        BigDecimal mockTotalPrice = new BigDecimal("15");

        given(this.userCartRespository.findItemsByUserid(1L)).willReturn(carts);
        given(this.userCartRespository.calculateTotalPriceByUserId(1L)).willReturn(mockTotalPrice);

        //when
        CartResponse actual = this.userCartService.getCartByUserId(1L);

        //then
        assertThat(actual.carts().size()).isEqualTo(2);
        assertTrue(actual.carts().contains(item1));
        assertTrue(actual.carts().contains(item2));
        assertThat(actual.totalPrice()).isEqualTo(mockTotalPrice);

        verify(this.userCartRespository, times(1)).findItemsByUserid(1L);
    }

    @Test
    void updateQuantityOfItem_withValidAmount_updatesCartItem() {
        // given
        UpdateItemRequest body = new UpdateItemRequest(1L, 5);

        Cart cart = Cart.builder()
                .id(1L)
                .userId(1L)
                .productId(1L)
                .quantity(1)
                .build();

        Cart updatedCart = Cart.builder()
                .id(1L)
                .userId(1L)
                .productId(1L)
                .quantity(5) // update amount quantity
                .build();

        given(userCartRespository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(cart));
        given(userCartRespository.save(cart)).willReturn(updatedCart);


        // when
        userCartService.updateQuantityOfItem(body, userDetail);

        // then
        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(userCartRespository).save(cartCaptor.capture());

        Cart savedCart = cartCaptor.getValue();
        assertEquals(1L, savedCart.getUserId());
        assertEquals(1L, savedCart.getProductId());
        assertEquals(5, savedCart.getQuantity());
    }

    @Test
    void updateQuantityOfItem_withNonCartItemId_throwsNotFoundException() {
        // given
        UpdateItemRequest body = new UpdateItemRequest(1L, 5);
        given(userCartRespository.findByIdAndUserId(1L, 1L))
                .willThrow(NotFoundException.class);

        // when
        assertThrows(NotFoundException.class, () -> {
            userCartService.updateQuantityOfItem(body, userDetail);
        });


        //verity
        verify(userCartRespository, times(1))
                .findByIdAndUserId(1L, 1L);

    }

    @Test
    void removeCartItem_withExistingCartItemId_removesItem() {
        // given
        Cart cart = Cart.builder()
                .id(1L)
                .userId(1L)
                .productId(1L)
                .quantity(1)
                .build();


        given(userCartRespository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(cart));
        doNothing().when(userCartRespository).deleteById(1L);

        // when
        userCartService.removeCartItem(1l, userDetail);

        // then
        verify(userCartRespository, times(1)).findByIdAndUserId(1L, 1L);

        verify(userCartRespository, times(1)).deleteById(1L);
    }

    @Test
    void removeCartItem_withNonCartItemId_throwsNotFoundException() {
        // given
        given(userCartRespository.findByIdAndUserId(1L, 1L))
                .willThrow(NotFoundException.class);

        // when
        assertThrows(NotFoundException.class, () -> {
            userCartService.removeCartItem(1L, userDetail);
        });


        // then
        verify(userCartRespository, times(1)).findByIdAndUserId(1L, 1L);

        verify(userCartRespository, never()).deleteById(1L);
    }
}