package com.dev.e_shop.product.Publics;

import com.dev.e_shop.dto.PaginationResponse;
import com.dev.e_shop.exception.NotFoundException;
import com.dev.e_shop.product.Product;
import com.dev.e_shop.product.ProductRepository;
import com.dev.e_shop.product.dto.ProductResponse;
import com.dev.e_shop.product.mapper.ProductMapper;
import com.dev.e_shop.product.publics.PublicProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class PublicProductServiceTest {

    @InjectMocks
    PublicProductService productService;

    @Mock
    ProductRepository productRepository;

    @Mock
    ProductMapper productMapper;

    @Test
    void getProductByPagination_Limit2Product_ShouldReturn2Products() {
        // Given
        int page = 0;
        int size = 2;

        List<Product> products = new ArrayList<>();
        Product product1 = Product.builder()
                .name("I phone 16")
                .price(BigDecimal.valueOf(500))
                .description("A new product")
                .brand("Apple")
                .imgUrl("/#")
                .build();

        products.add(product1);
        products.add(product1);
        products.add(product1);

        // simulate query get limit
        int start = page * size;
        int end = Math.min(start + size, products.size());
        List<Product> pagedList = products.subList(start, end);
        Page<Product> productPage = new PageImpl<>(pagedList, PageRequest.of(page, size), products.size());

        given(productRepository.findAll(any(Pageable.class))).willReturn(productPage);

        // When
        Map<String, Object> actual = productService.getProductsByPagination(page, size);

        // Then
        assertNotNull(actual);
        assertThat(actual).containsKeys("products", "pagination");
        assertThat(actual.get("products")).isInstanceOf(List.class);
        assertThat(((List<?>) actual.get("products")).size()).isEqualTo(size);

        PaginationResponse pagination = (PaginationResponse) actual.get("pagination");
        assertThat(pagination.currentPage()).isEqualTo(page);
        assertThat(pagination.totalPage()).isEqualTo(2);
        assertThat(pagination.totalItems()).isEqualTo(products.size());
        assertThat(pagination.pageSize()).isEqualTo(size);

        verify(productRepository, times(1)).findAll(PageRequest.of(page, size));
    }

    @Test
    void getById_ExistingId_ShouldReturnConcreteProduct() {
        // Given
        Product product1 = Product.builder()
                .name("I phone 16")
                .price(BigDecimal.valueOf(500))
                .description("A new product")
                .brand("Apple")
                .imgUrl("/#")
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
        given(productRepository.findById(1L)).willReturn(Optional.of(product1));
        given(productMapper.toProductResponse(any(Product.class))).willReturn(response);

        // When
        ProductResponse actual = productService.getById(1L);

        // Then
        assertThat(actual.id()).isEqualTo(response.id());
        assertThat(actual.name()).isEqualTo(response.name());

        verify(productRepository, times(1)).findById(1L);
        verify(productMapper, times(1)).toProductResponse(product1);
    }

    @Test
    void getById_NotFoundId_ThrowNotFoundException() {
        // Given
        given(productRepository.findById(1L)).willReturn(Optional.empty());

        // When
        assertThrows(NotFoundException.class, () -> {
            productService.getById(1L);
        });

        // Then
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void testGetProductByName_ExistingName_ShouldReturnProduct() {
        //given
        String name = "iphone 15";
        Product product1 = Product.builder()
                .name("I phone 16")
                .price(BigDecimal.valueOf(500))
                .description("A new product")
                .brand("Apple")
                .imgUrl("/#")
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
        given(productRepository.findByNameIgnoreCase(name)).willReturn(Optional.of(product1));
        given(productMapper.toProductResponse(any(Product.class))).willReturn(response);
        //when

        var actual = productService.getProductByName(name);

        //then
        assertThat(actual.name()).isEqualTo(response.name());

        verify(productRepository, times(1)).findByNameIgnoreCase(name);
        verify(productMapper, times(1)).toProductResponse(product1);
    }

    @Test
    void productByName_NotFoundName_ThrowNotFoundException() {
        //given
        String name = "iphone 15";

        given(productRepository.findByNameIgnoreCase(name)).willReturn(Optional.empty());

        //when
        assertThrows(NotFoundException.class,() -> {
            var actual = productService.getProductByName(name);
        });

        //then
        verify(productRepository, times(1)).findByNameIgnoreCase(name);
    }

    @Test
    void getProductContainByName_Success_ShouldReturnProduct() {
        //given
        String name = "Iphone";
        int page = 0;
        int size = 1;
        Product product1 = Product.builder()
                .name("Iphone 16")
                .price(BigDecimal.valueOf(500))
                .description("A new product")
                .brand("Apple")
                .imgUrl("/#")
                .build();
        Product product2 = Product.builder()
                .name("Iphone 17")
                .price(BigDecimal.valueOf(500))
                .description("A new product")
                .brand("Apple")
                .imgUrl("/#")
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

        List<Product> products = Arrays.asList(product1, product2);


        // simulate query get limit
        int start = page * size;
        int end = Math.min(start + size, products.size());
        List<Product> pagedList = products.subList(start, end);
        Page<Product> productPage = new PageImpl<>(pagedList, PageRequest.of(page, size), products.size());
        given(productRepository.findByNameContainingIgnoreCase(any(String.class), any(Pageable.class)))
                .willReturn(productPage);

        //when
        Map<String, Object> actual = productService.getProductContainByName(name, page, size);

        //then
        assertNotNull(actual);
        assertThat(actual).containsKeys("products", "pagination");
        assertThat(actual.get("products")).isInstanceOf(List.class);
        assertThat(((List<?>) actual.get("products")).size()).isEqualTo(size);

        PaginationResponse pagination = (PaginationResponse) actual.get("pagination");
        assertThat(pagination.currentPage()).isEqualTo(page);
        assertThat(pagination.totalPage()).isEqualTo(2);
        assertThat(pagination.totalItems()).isEqualTo(products.size());
        assertThat(pagination.pageSize()).isEqualTo(size);
    }
}