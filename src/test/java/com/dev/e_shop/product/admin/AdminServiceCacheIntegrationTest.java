package com.dev.e_shop.product.admin;

import com.dev.e_shop.product.Product;
import com.dev.e_shop.product.ProductRepository;
import com.dev.e_shop.product.dto.CreateProductRequest;
import com.dev.e_shop.product.dto.ProductResponse;
import com.dev.e_shop.product.dto.UpdateProductRequest;
import com.dev.e_shop.product.mapper.ProductMapper;
import com.dev.e_shop.product.publics.PublicProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.parameters.P;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ActiveProfiles("test")
public class AdminServiceCacheIntegrationTest {
    @Autowired
    AdminProductService adminProductService;

    @MockitoSpyBean
    ProductRepository productRepository;

    @MockitoSpyBean
    ProductMapper productMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    void updateProduct_whenUpdated_removeRelatedCache() {
        //given
        String keyById = "product::1";
        String keyByPagination = "products::0:5";

        CreateProductRequest body = new CreateProductRequest(
                "Iphone 16",
                new BigDecimal("300.0"),
                "A new phone is...",
                1,
                "Apple",
                "/#"
        );

        Product product = Product.builder()
                .name(body.getName())
                .price(body.getPrice())
                .description(body.getDescription())
                .brand(body.getBrand())
                .imgUrl(body.getImgUrl())
                .build();


        given(this.productRepository.findById(1L))
                .willReturn(Optional.of(product));

        given(this.productRepository.save(product))
                .willReturn(new Product());
        //when
        this.adminProductService.updateProduct(1, UpdateProductRequest.builder().build());

        //then
        Boolean hasKeyById = redisTemplate.hasKey(keyById);
        Boolean hasKeyPagination = redisTemplate.hasKey(keyByPagination);

        assertThat(hasKeyById).isFalse();
        assertThat(hasKeyPagination).isFalse();
    }

    @Test
    void create_whenAdded_refreshCacheWithLastedProducts() {
        // given
        String keyByPagination = "products::0:5";
        CreateProductRequest body = new CreateProductRequest(
                "Iphone 18",
                new BigDecimal("300.0"),
                "A new phone is...",
                1,
                "Apple",
                "/#"
        );

        Product product = Product.builder()
                .name(body.getName())
                .price(body.getPrice())
                .description(body.getDescription())
                .brand(body.getBrand())
                .imgUrl(body.getImgUrl())
                .build();

        Product savedProduct = Product.builder()
                .id(1)
                .name(body.getName())
                .price(body.getPrice())
                .description(body.getDescription())
                .brand(body.getBrand())
                .imgUrl(body.getImgUrl())
                .build();

        ProductResponse response = new ProductResponse(
                1,
                "Iphone 16",
                new BigDecimal("300.0"),
                "A new phone is...",
                1,
                "Apple",
                "/#"
        );

        given(productMapper.toProduct(body)).willReturn(product);
        given(productRepository.save(product)).willReturn(savedProduct);
        given(productMapper.toProductResponse(savedProduct)).willReturn(response);


        // when
        this.adminProductService.create(body);

        // then
        Boolean hasKeyPagination = redisTemplate.hasKey(keyByPagination);

        // Assert cache đã được xóa
        assertThat(hasKeyPagination).isFalse();
    }

    @Test
    void remove_whenRemoved_removeRelatedCache() {
        //given
        String keyById = "product::1";

        //when
        this.adminProductService.remove(1);

        //then
        Boolean hasKeyPagination = redisTemplate.hasKey(keyById);

        assertThat(hasKeyPagination).isFalse();
    }
}
