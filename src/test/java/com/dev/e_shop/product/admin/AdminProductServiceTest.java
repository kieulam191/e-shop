package com.dev.e_shop.product.admin;

import com.dev.e_shop.exception.custom.NotFoundException;
import com.dev.e_shop.product.Product;
import com.dev.e_shop.product.ProductRepository;
import com.dev.e_shop.product.dto.CreateProductRequest;
import com.dev.e_shop.product.dto.ProductResponse;
import com.dev.e_shop.product.dto.UpdateProductRequest;
import com.dev.e_shop.product.dto.StockProductDto;
import com.dev.e_shop.product.mapper.ProductMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminProductServiceTest {

    @InjectMocks
    AdminProductService productService;

    @Mock
    ProductRepository productRepository;

    @Mock
    ProductMapper productMapper;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void create_withValidInput_returnsProductResponse() {
        //given
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

        // Act
        ProductResponse actual = productService.create(body);

        // Assert
        assertNotNull(actual);
        assertThat(actual.name()).isEqualTo(response.name());
        assertThat(actual.price()).isEqualTo(response.price());
        assertThat(actual.description()).isEqualTo(response.description());

        verify(this.productMapper, times(1)).toProduct(body);
        verify(this.productRepository, times(1)).save(product);
        verify(this.productMapper, times(1)).toProductResponse(savedProduct);
    }

    @Test
    void updateProductInfo_withExistingId_returnsProductResponse() {
        UpdateProductRequest body = UpdateProductRequest.builder()
                .name("Iphone 16 pro")
                .price(new BigDecimal(500.0))
                .build();

        Product product = Product.builder()
                .id(1)
                .name("Iphone 16")
                .price(new BigDecimal(400))
                .brand("Apple")
                .build();

        Product updatedProduct = Product.builder()
                .id(1)
                .name("Iphone 16 pro")
                .price(new BigDecimal(500))
                .brand("Apple")
                .build();

        ProductResponse response = new ProductResponse(
                1,
                "Iphone 16 pro",
                new BigDecimal("500"),
                "A new phone is...",
                1,
                "Apple",
                "/#"
        );

        given(this.productRepository.findById(1L)).willReturn(Optional.of(product));
        given(this.productRepository.save(product)).willReturn(updatedProduct);
        given(this.productMapper.toProductResponse(updatedProduct)).willReturn(response);

        //when
        ProductResponse actual = this.productService.updateProduct(1L, body);

        //then
        assertNotNull(actual);
        assertThat(actual.id()).isEqualTo(response.id());
        assertThat(actual.name()).isEqualTo(response.name());
        assertThat(actual.price()).isEqualTo(response.price());
        assertThat(actual.brand()).isEqualTo(response.brand());

        verify(this.productRepository, times(1)).findById(1L);
        verify(this.productRepository, times(1)).save(product);
        verify(this.productMapper, times(1)).toProductResponse(updatedProduct);

    }

    @Test
    void updateProductInfo_withNotFoundId_throwsNotFoundException() {
        //given
        UpdateProductRequest body = UpdateProductRequest.builder()
                .build();


        given(this.productRepository.findById(1L)).willReturn(Optional.empty());

        // When
        assertThrows(NotFoundException.class, () -> {
            productService.updateProduct(1L, body);
        });

        //then
        verify(this.productRepository, times(1)).findById(1L);
    }

    @Test
    void updateStockById_withExistingId_returnsProductResponse() {
        //given
        StockProductDto body = new StockProductDto(10);
        body.setStock(100);

        Product product = Product.builder()
                        .stock(10)
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



        given(this.productRepository.findById(1L)).willReturn(Optional.of(product));
        given(this.productRepository.save(product)).willReturn(product);

        // When
        StockProductDto actual = productService.updateStockById(1L, body);

        assertThat(actual.getStock()).isEqualTo(110); // update amount of stock

        //then
        verify(this.productRepository, times(1)).findById(1L);
        verify(this.productRepository, times(1)).save(product);
    }

    @Test
    void remove_withExistingId_setsDeletedFlagToTrue() {
        //given
        Product product = Product.builder()
                .isDeleted(false)
                .build();


        given(this.productRepository.findById(1L)).willReturn(Optional.of(product));
        given(this.productRepository.save(product)).willReturn(product);
        //when
        this.productService.remove(1L);

        //then
        assertTrue(product.isDeleted());

        verify(this.productRepository, times(1)).findById(1L);
        verify(this.productRepository, times(1)).save(product);
    }

    @Test
    void remove_withNotFoundId_doesNothing() {
        //given
        Product product = Product.builder()
                .isDeleted(false)
                .build();


        given(this.productRepository.findById(1L)).willReturn(Optional.empty());
        //when
        assertThrows(NotFoundException.class, () -> {
            this.productService.remove(1L);
        });

        assertFalse(product.isDeleted());

        //verify
        verify(this.productRepository, times(1)).findById(1L);
        verify(this.productRepository, never()).save(product);
    }

    @Test
    void getStockInfo_withExistingId_returnsStockProductDto() {
        //given
        given(this.productRepository.findStockViewById(1L))
                .willReturn(Optional.of(new ProductRepository.StockView() {
                    @Override
                    public Long getId() {
                        return 1L;
                    }

                    @Override
                    public String getName() {
                        return "test";
                    }

                    @Override
                    public int getStock() {
                        return 50;
                    }
                }));
        //when
        ProductRepository.StockView actual = this.productService.getStockInfo(1L);
        //then
        assertNotNull(actual);
        assertThat(actual.getId()).isEqualTo(1L);
        assertThat(actual.getStock()).isEqualTo(50);

        verify(this.productRepository, times(1)).findStockViewById(1L);
    }

    @Test
    void getStockInfo_withNotFoundId_throwsNotFoundException() {
        //given
        given(this.productRepository.findStockViewById(1L))
                .willReturn(Optional.empty());
        //when
        assertThrows(NotFoundException.class, () -> {
            ProductRepository.StockView actual = this.productService.getStockInfo(1L);
        });

        verify(this.productRepository, times(1)).findStockViewById(1L);
    }
}