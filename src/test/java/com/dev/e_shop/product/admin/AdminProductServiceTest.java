package com.dev.e_shop.product;

import com.dev.e_shop.dto.PaginationResponse;
import com.dev.e_shop.exception.NotFoundException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    ProductService productService;

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
    void create_ValidInput_ShouldCreatingProduct() {
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
    void update_ValidInput_ShouldUpdatingProduct() {
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
    void update_NotFoundId_ThrowNotFoundException() {
        //given
        UpdateProductRequest body = UpdateProductRequest.builder()
                .name("Iphone 16 pro")
                .price(new BigDecimal(500.0))
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
    void updateStockById_ValidInput_ShouldUpdateStock() {
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
    void remove_ExistingId_ShouldChangeProductStatus() {
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
    void remove_NotFoundId_ThrowNotFoundException() {
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
    void getStockInfo_ExistingId_ShouldReturnStockInfo() {
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
    void getStockInfo_NotFoundId_ThrowNotFoundException() {
        //given
        given(this.productRepository.findStockViewById(1L))
                .willReturn(Optional.empty());
        //when
        assertThrows(NotFoundException.class, () -> {
            ProductRepository.StockView actual = this.productService.getStockInfo(1L);
        });

        verify(this.productRepository, times(1)).findStockViewById(1L);
    }
//
//    @Test
//    void getProductByPagination_Limit2Product_ShouldReturn2Products() {
//        // Given
//        int page = 0;
//        int size = 2;
//
//        List<Product> products = new ArrayList<>();
//        Product product1 = Product.builder()
//                .name("I phone 16")
//                .price(BigDecimal.valueOf(500))
//                .description("A new product")
//                .brand("Apple")
//                .imgUrl("/#")
//                .build();
//
//        products.add(product1);
//        products.add(product1);
//        products.add(product1);
//
//        // simulate query get limit
//        int start = page * size;
//        int end = Math.min(start + size, products.size());
//        List<Product> pagedList = products.subList(start, end);
//        Page<Product> productPage = new PageImpl<>(pagedList, PageRequest.of(page, size), products.size());
//
//        given(productRepository.findAll(any(Pageable.class))).willReturn(productPage);
//
//        // When
//        Map<String, Object> actual = productService.getProductsByPagination(page, size);
//
//        // Then
//        assertNotNull(actual);
//        assertThat(actual).containsKeys("products", "pagination");
//        assertThat(actual.get("products")).isInstanceOf(List.class);
//        assertThat(((List<?>) actual.get("products")).size()).isEqualTo(size);
//
//        PaginationResponse pagination = (PaginationResponse) actual.get("pagination");
//        assertThat(pagination.currentPage()).isEqualTo(page);
//        assertThat(pagination.totalPage()).isEqualTo(2);
//        assertThat(pagination.totalItems()).isEqualTo(products.size());
//        assertThat(pagination.pageSize()).isEqualTo(size);
//
//        verify(productRepository, times(1)).findAll(PageRequest.of(page, size));
//    }
//
//    @Test
//    void getById_ExistingId_ShouldReturnConcreteProduct() {
//        // Given
//        Product product1 = Product.builder()
//                .name("I phone 16")
//                .price(BigDecimal.valueOf(500))
//                .description("A new product")
//                .brand("Apple")
//                .imgUrl("/#")
//                .build();
//
//        ProductResponse response = new ProductResponse(
//                1,
//                "Iphone 16",
//                new BigDecimal("300.0"),
//                "A new phone is...",
//                1,
//                "Apple",
//                "/#"
//        );
//        given(productRepository.findById(1L)).willReturn(Optional.of(product1));
//        given(productMapper.toProductResponse(any(Product.class))).willReturn(response);
//
//        // When
//        ProductResponse actual = productService.getById(1L);
//
//        // Then
//        assertThat(actual.id()).isEqualTo(response.id());
//        assertThat(actual.name()).isEqualTo(response.name());
//
//        verify(productRepository, times(1)).findById(1L);
//        verify(productMapper, times(1)).toProductResponse(product1);
//    }
//
//    @Test
//    void getById_NotFoundId_ThrowNotFoundException() {
//        // Given
//        given(productRepository.findById(1L)).willReturn(Optional.empty());
//
//        // When
//        assertThrows(NotFoundException.class, () -> {
//            productService.getById(1L);
//        });
//
//        // Then
//        verify(productRepository, times(1)).findById(1L);
//    }
//
//    @Test
//    void testGetProductByName_ExistingName_ShouldReturnProduct() {
//        //given
//        String name = "iphone 15";
//        Product product1 = Product.builder()
//                .name("I phone 16")
//                .price(BigDecimal.valueOf(500))
//                .description("A new product")
//                .brand("Apple")
//                .imgUrl("/#")
//                .build();
//        ProductResponse response = new ProductResponse(
//                1,
//                "Iphone 16",
//                new BigDecimal("300.0"),
//                "A new phone is...",
//                1,
//                "Apple",
//                "/#"
//        );
//        given(productRepository.findByNameIgnoreCase(name)).willReturn(Optional.of(product1));
//        given(productMapper.toProductResponse(any(Product.class))).willReturn(response);
//        //when
//
//        var actual = productService.getProductByName(name);
//
//        //then
//        assertThat(actual.name()).isEqualTo(response.name());
//
//        verify(productRepository, times(1)).findByNameIgnoreCase(name);
//        verify(productMapper, times(1)).toProductResponse(product1);
//    }
//
//    @Test
//    void productByName_NotFoundName_ThrowNotFoundException() {
//        //given
//        String name = "iphone 15";
//
//        given(productRepository.findByNameIgnoreCase(name)).willReturn(Optional.empty());
//
//        //when
//        assertThrows(NotFoundException.class,() -> {
//            var actual = productService.getProductByName(name);
//        });
//
//        //then
//        verify(productRepository, times(1)).findByNameIgnoreCase(name);
//    }
//
//    @Test
//    void getProductContainByName_Success_ShouldReturnProduct() {
//        //given
//        String name = "Iphone";
//        int page = 0;
//        int size = 1;
//        Product product1 = Product.builder()
//                .name("Iphone 16")
//                .price(BigDecimal.valueOf(500))
//                .description("A new product")
//                .brand("Apple")
//                .imgUrl("/#")
//                .build();
//        Product product2 = Product.builder()
//                .name("Iphone 17")
//                .price(BigDecimal.valueOf(500))
//                .description("A new product")
//                .brand("Apple")
//                .imgUrl("/#")
//                .build();
//        ProductResponse response = new ProductResponse(
//                1,
//                "Iphone 16",
//                new BigDecimal("300.0"),
//                "A new phone is...",
//                1,
//                "Apple",
//                "/#"
//        );
//
//        List<Product> products = Arrays.asList(product1, product2);
//
//
//        // simulate query get limit
//        int start = page * size;
//        int end = Math.min(start + size, products.size());
//        List<Product> pagedList = products.subList(start, end);
//        Page<Product> productPage = new PageImpl<>(pagedList, PageRequest.of(page, size), products.size());
//        given(productRepository.findByNameContainingIgnoreCase(any(String.class), any(Pageable.class)))
//                .willReturn(productPage);
//
//        //when
//        Map<String, Object> actual = productService.getProductContainByName(name);
//
//        //then
//        assertNotNull(actual);
//        assertThat(actual).containsKeys("products", "pagination");
//        assertThat(actual.get("products")).isInstanceOf(List.class);
//        assertThat(((List<?>) actual.get("products")).size()).isEqualTo(size);
//
//        PaginationResponse pagination = (PaginationResponse) actual.get("pagination");
//        assertThat(pagination.currentPage()).isEqualTo(page);
//        assertThat(pagination.totalPage()).isEqualTo(2);
//        assertThat(pagination.totalItems()).isEqualTo(products.size());
//        assertThat(pagination.pageSize()).isEqualTo(size);
//    }
}