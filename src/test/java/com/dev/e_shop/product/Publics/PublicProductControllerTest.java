package com.dev.e_shop.product.Publics;

import com.dev.e_shop.exception.custom.NotFoundException;
import com.dev.e_shop.product.Product;
import com.dev.e_shop.product.dto.ProductPreviewResponse;
import com.dev.e_shop.product.dto.ProductResponse;
import com.dev.e_shop.product.publics.PublicProductService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class PublicProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PublicProductService productService;


    private Product product1, product2;
    private List<Product> products;
    private List<ProductResponse> productResponses;
    private ProductResponse productResponse1, productResponse2;
    private ProductPreviewResponse productPreviewResponse1, productPreviewResponse2;

    @BeforeEach
    void setUp() {
        //given
        product1 = new Product();
        product1.setId(1L);
        product1.setName("iphone 15");
        product1.setBrand("Apple");
        product1.setPrice(BigDecimal.valueOf(500.0));
        product1.setDescription("unknow");
        product1.setImgUrl("/");
        product1.setDeleted(false);

        product2 = new Product();
        product2.setId(1L);
        product2.setName("iphone 15");
        product2.setBrand("Apple");
        product2.setPrice(BigDecimal.valueOf(500.0));
        product2.setDescription("unknow");
        product2.setImgUrl("/");
        product2.setDeleted(false);

        products = new ArrayList<>();
        products.add(product1);
        products.add(product2);

        productResponse1 = new ProductResponse(
                1,
                "Iphone 16",
                new BigDecimal("300.0"),
                "A new phone is...",
                1,
                "Apple",
                "/#"
        );

        productResponse2 = new ProductResponse(
                2,
                "Iphone 16 pro",
                new BigDecimal("300.0"),
                "A new phone is...",
                1,
                "Apple",
                "/#"
        );

        productPreviewResponse1 = new ProductPreviewResponse(
                1,
                "Iphone 16",
                new BigDecimal("300.0")
        );

        productPreviewResponse2 = new ProductPreviewResponse(
                2,
                "Iphone 16",
                new BigDecimal("300.0")
        );

        productResponses = new ArrayList<>();
        productResponses.add(productResponse1);
        productResponses.add(productResponse2);
    }

    @Test
    void getProductsByPagination_withLimit2_returnsProductPreviewResponse() throws Exception {
        //given
        int page = 0, size = 1;

        Map<String, Number> infoPage = new HashMap<>();
        infoPage.put("currentPage", 0);
        infoPage.put("totalPage", 0);
        infoPage.put("totalItems", 2);
        infoPage.put("pageSize", 1);

        Map<String, Object> data = new HashMap<>();
        data.put("products", productResponses);
        data.put("pagination", infoPage);

        given(this.productService.getProductsByPagination(page, size)).willReturn(data);

        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/public/products/")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get products success"))
                .andExpect(jsonPath("$.data", Matchers.hasKey("products")))
                .andExpect(jsonPath("$.data", Matchers.hasKey("pagination")))
                .andExpect(jsonPath("$.data.products", Matchers.hasSize(2)));
    }

    @Test
    void getProductsBySearching_withValidName_returnsProductPreviewResponse() throws Exception {
        //given
        int page = 0, size = 1;

        Map<String, Number> infoPage = new HashMap<>();
        infoPage.put("currentPage", 0);
        infoPage.put("totalPage", 0);
        infoPage.put("totalItems", 2);
        infoPage.put("pageSize", 1);

        Map<String, Object> data = new HashMap<>();
        data.put("products", List.of(productPreviewResponse1, productPreviewResponse2));
        data.put("pagination", infoPage);

        given(this.productService.getProductContainByName(any(String.class), eq(page), eq(size)))
                .willReturn(data);

        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/public/products/search")
                        .param("name", "iphone")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get products success"))
                .andExpect(jsonPath("$.data", Matchers.hasKey("products")))
                .andExpect(jsonPath("$.data", Matchers.hasKey("pagination")))
                .andExpect(jsonPath("$.data.products", Matchers.hasSize(2)));
    }

    @Test
    void getProductById_withExistingId_returnsConcreteProduct() throws Exception {
        //given

        given(this.productService.getProductDetailById(1L)).willReturn(productResponse1);


        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/public/products/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get product success"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("Iphone 16"));
    }

    @Test
    void getProductById_withNotFoundId_throwsNotFoundException() throws Exception {
        //given
        given(this.productService.getProductDetailById(1L)).willThrow(
                new NotFoundException("Product with ID 1 not found"));

        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/public/products/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Resource not found"))
                .andExpect(jsonPath("$.errors[0]").value("Product with ID 1 not found"));
    }
}