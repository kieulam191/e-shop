package com.dev.e_shop.cart;

import com.dev.e_shop.cart.dto.AddItemRequest;
import com.dev.e_shop.cart.dto.CartDto;
import com.dev.e_shop.cart.dto.CartResponse;
import com.dev.e_shop.cart.dto.UpdateItemRequest;
//import com.dev.e_shop.config.EmbeddedRedisConfig;
import com.dev.e_shop.config.EmbeddedRedisConfig;
import com.dev.e_shop.product.Product;
import com.dev.e_shop.product.ProductRepository;
import com.dev.e_shop.user.User;
import com.dev.e_shop.user.UserDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(EmbeddedRedisConfig.class)
@TestPropertySource(properties = "spring.main.allow-bean-definition-overriding=true")
public class UserCartServiceIntegrationTest {

    @Autowired
    UserCartService userCartService;

    @MockitoSpyBean
    UserCartRepository userCartRepository;

    @MockitoSpyBean
    ProductRepository productRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private UserDetail userDetail;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        User user = new User();
        user.setEmail("test@gmail.com");
        user.setId(1L);

        userDetail = new UserDetail(user);
    }

    @Test
    @Transactional
    @Rollback
    void addCartItem_whenAdded_refreshesCacheWithLatestCart() {
        // given
        String cacheKey = "cart::1";

        AddItemRequest body = new AddItemRequest(3L);

        Cart cart = new Cart();

        given(this.productRepository.findById(3L)).willReturn(Optional.of(new Product()));
        given(this.userCartRepository.findByProductIdAndUserId(3L, 1L)).willReturn(Optional.of(cart));
        given(this.userCartRepository.save(cart)).willReturn(new Cart());

        // when
        CartResponse response = userCartService.addCartItem(body, userDetail);

        // then
        CartResponse cache = (CartResponse) redisTemplate.opsForValue().get(cacheKey);

        assertThat(cache).isNotNull();
        assertEquals(cache, response);

        verify(this.userCartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    @Transactional
    @Rollback
    void updateQuantityOfItem_whenUpdated_refreshesCacheWithLatestCart() {
        //given
        String cacheKey = "cart::1";

        CartDto existsItem = new CartDto();
        existsItem.setId(1L);
        existsItem.setQuantity(1);

        UpdateItemRequest body = new UpdateItemRequest(1L, 5);

        redisTemplate.opsForValue().set(cacheKey, new CartResponse(Set.of(existsItem), BigDecimal.ZERO));

        given(this.userCartRepository.findByIdAndUserId(anyLong(), anyLong()))
                .willReturn(Optional.of(new Cart()));
        given(this.userCartRepository.save(new Cart())).willReturn(new Cart());

        //when
        CartResponse response = userCartService.updateQuantityOfItem(body, userDetail);

        //then
        CartResponse cache = (CartResponse) redisTemplate.opsForValue().get(cacheKey);

        assertEquals(cache, response);
        verify(this.userCartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void removeCartItem_whenRemoved_refreshesCacheWithLatestCart() {
        //given
        String cacheKey = "cart::1";
        given(this.userCartRepository.findByIdAndUserId(anyLong(), anyLong()))
                .willReturn(Optional.of(new Cart()));

        // when
        CartResponse response = userCartService.removeCartItem(1L, userDetail);

        // then
        CartResponse cache = (CartResponse) redisTemplate.opsForValue().get(cacheKey);

        assertThat(cache).isEqualTo(response);
        verify(this.userCartRepository, times(1)).deleteById(1L);
    }

    @Test
    void getCartByUserId_whenHit_refreshesCacheWithLatestCart() {
        //given
        String cacheKey = "cart::1";
        redisTemplate.opsForValue().set(cacheKey, new CartResponse(Set.of(), BigDecimal.ZERO));

        //when
        CartResponse response = this.userCartService.getCartByUserId(1L);

        //then
        CartResponse cacheCart =(CartResponse) redisTemplate.opsForValue().get(cacheKey);

        assertThat(cacheCart).isEqualTo(response);
        verify(this.userCartRepository, never()).findItemsByUserid(1L);
    }

    @Test
    void getCartByUserId_whenMiss_refreshesCacheWithLatestCart() {
        //given
        String cacheKey = "cart::1";
        redisTemplate.delete(cacheKey);

        //when
        CartResponse response = this.userCartService.getCartByUserId(1L);

        //then
        CartResponse cache = (CartResponse) redisTemplate.opsForValue().get(cacheKey);

        assertThat(cache).isEqualTo(response);
        verify(this.userCartRepository, times(1)).findItemsByUserid(1L);
    }
}
