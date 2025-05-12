package com.dev.e_shop.product.Publics;

import com.dev.e_shop.product.ProductRepository;
import com.dev.e_shop.product.dto.ProductResponse;
import com.dev.e_shop.product.publics.PublicProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class ProductServiceCacheIntegrationTest {

    @Autowired
    PublicProductService publicProductService;

    @MockitoSpyBean
    ProductRepository productRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    void getProductsByPagination_whenCacheHit_returnsProductsFromCache() {
        //given
        String cacheKey = "products::0:5";

        Map<String, Object> data = new HashMap<>();
        data.put("products", List.of());
        data.put("pagination", "dummy");

        redisTemplate.opsForValue().set(cacheKey, data);

        //when
        Map<String, Object> productsByPagination = publicProductService.getProductsByPagination(0, 5);

        //then
        Map<String, Object> cache = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
        assertThat(productsByPagination).isEqualTo(cache);

        verify(productRepository, never()).findAll(any(PageRequest.class));
    }
    @Test
    void getProductsByPagination_whenCacheMiss_returnsProductsFromDb() throws InterruptedException {
        //given
        String cacheKey = "products::0:5";
        redisTemplate.delete(cacheKey);

        //when
        Map<String, Object> response = publicProductService.getProductsByPagination(0, 5);

        //then
        boolean existsCacheKey = redisTemplate.hasKey(cacheKey);
        assertTrue(existsCacheKey, "Should create cache key after miss");

        Map<String, Object>  cache = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);

        assertThat(cache).isEqualTo(response);
        verify(productRepository, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    void getProductDetailById_whenCacheHit_returnsProductFromCache() {
        //given
        String cacheKey = "product::1";

        ProductResponse productResponse = new ProductResponse(
                1,
                "Iphone 16",
                new BigDecimal("300.0"),
                "A new phone is...",
                1,
                "Apple",
                "/#"
        );
        redisTemplate.opsForValue().set(cacheKey,  productResponse);

        // when
        ProductResponse response = publicProductService.getProductDetailById(1L);

        //then
        ProductResponse cache =(ProductResponse) redisTemplate.opsForValue().get(cacheKey);

        assertThat(cache).isEqualTo(response);
        verify(productRepository, never()).findById(1L);
    }

    @Test
    void getProductDetailById_whenCacheMiss_returnsProductFromDb() throws InterruptedException {
        //given
        String cacheKey = "product::1";
        redisTemplate.delete(cacheKey);

        //when
        ProductResponse response = publicProductService.getProductDetailById(1L);

        //then
        ProductResponse cache = (ProductResponse) redisTemplate.opsForValue().get(cacheKey);
        assertThat(cache).isEqualTo(response);

        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProductContainByName_whenCacheHit_returnsProductFromCache() {
        //given
        String cacheKey = "products::0:5:Iphone";

        Map<String, Object> data = new HashMap<>();
        data.put("products", List.of());
        data.put("pagination", "dummy");

        redisTemplate.opsForValue().set(cacheKey, data);

        //when
        Map<String, Object> respnose = publicProductService.getProductContainByName("Iphone", 0, 5);

        //then
        Map<String, Object> cache = (Map<String, Object>)redisTemplate.opsForValue().get(cacheKey);
        assertThat(cache).isEqualTo(respnose);

        verify(productRepository, never()).findByNameContainingIgnoreCase(eq("Iphone"), any(PageRequest.class));
    }

    @Test
    void getProductContainByName_whenCacheMiss_returnsProductFromDb() {
        //given
        String cacheKey = "products::0:5:Iphone";
        redisTemplate.delete(cacheKey);

        //when
        Map<String, Object> response = publicProductService.getProductContainByName("Iphone", 0, 5);

        //then
        Map<String, Object> cache = (Map<String, Object>)redisTemplate.opsForValue().get(cacheKey);
        assertThat(cache).isEqualTo(cache);
        verify(productRepository, times(1)).findByNameContainingIgnoreCase(eq("Iphone"), any(PageRequest.class));
    }
}
