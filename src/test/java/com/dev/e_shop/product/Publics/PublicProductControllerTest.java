package com.dev.e_shop.product;

import com.dev.e_shop.exception.NotFoundException;
import com.dev.e_shop.product.dto.ProductResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class UserProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProductService productService;

    @Autowired
    ObjectMapper objectMapper;

    private Product product1, product2;
    private List<Product> products;
    private List<ProductResponse> productResponses;
    private ProductResponse productResponse1, productResponse2;

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
                1,
                "Iphone 16",
                new BigDecimal("300.0"),
                "A new phone is...",
                1,
                "Apple",
                "/#"
        );


        productResponses = new ArrayList<>();
        productResponses.add(productResponse1);
        productResponses.add(productResponse2);
    }

    @Test
    void getProductsByPagination_Success_ShouldReturnProductsByPagination() throws Exception {
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
    void getProductsBySearching_Success_ShouldReturnProductsByPagination() throws Exception {
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

        given(this.productService.getProductContainByName(any(String.class))).willReturn(data);

        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/public/products/test/search")
                        .param("name", "iphone")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get products success"))
                .andExpect(jsonPath("$.data", Matchers.hasKey("products")))
                .andExpect(jsonPath("$.data", Matchers.hasKey("pagination")))
                .andExpect(jsonPath("$.data.products", Matchers.hasSize(2)));
    }

    @Test
    void getProductById_ExistingId_ShouldReturnConcreteProduct() throws Exception {
        //given

        given(this.productService.getById(1L)).willReturn(productResponse1);


        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/public/products/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get product success"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("Iphone 16"));
    }

    @Test
    void getProductById_NotFoundId_ThrowNotFoundException() throws Exception {
        //given
        given(this.productService.getById(1L)).willThrow(new NotFoundException("Resource not found"));

        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/public/products/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Resource not found"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void getProductByName_searchWithName_ShouldReturnProduct() throws Exception {
        //given
        given(this.productService.getProductByName(any(String.class))).willReturn(productResponse1);

        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/public/products/search")
                        .param("name", "iphone 15")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get product success"))
                .andExpect(jsonPath("$.data.id").value(productResponse1.id()))
                .andExpect(jsonPath("$.data.name").value(productResponse1.name()));
    }

    @Test
    void getProductByName_searchWithoutCaseSensitiveName_ShouldReturnProduct() throws Exception {
        //given
        given(this.productService.getProductByName(any(String.class))).willReturn(productResponse1);

        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/public/products/search")
                        .param("name", "IPHONE 15")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get product success"))
                .andExpect(jsonPath("$.data.id").value(productResponse1.id()))
                .andExpect(jsonPath("$.data.name").value(productResponse1.name()));
    }

    @Test
    void getProductByName_NotFoundName_ThrowNotFoundException() throws Exception {
        //given
        given(this.productService.getProductByName(any(String.class))).willThrow(new NotFoundException("Resource not found"));

        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/public/products/search")
                        .param("name", "iphone 15")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Resource not found"))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}