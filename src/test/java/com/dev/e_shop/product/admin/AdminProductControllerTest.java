package com.dev.e_shop.product.admin;

import com.dev.e_shop.exception.NotFoundException;
import com.dev.e_shop.product.ProductRepository;
import com.dev.e_shop.product.dto.CreateProductRequest;
import com.dev.e_shop.product.dto.ProductResponse;
import com.dev.e_shop.product.dto.UpdateProductRequest;
import com.dev.e_shop.product.dto.StockProductDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AdminProductControllerTest {

    @MockitoBean
    AdminProductService productService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void create_ValidInput_ShouldReturnResponse() throws Exception {
       //given
        CreateProductRequest body = new CreateProductRequest(
                "Iphone 16",
                new BigDecimal("300.0"),
                "A new phone is...",
                1,
                "Apple",
                "/#"
        );

        ProductResponse response = new ProductResponse(
                1,
                "Iphone 16",
                new BigDecimal("300.0"),
                "A new phone is...",
                1,
                "Apple",
                "/#"
        );

        String json = objectMapper.writeValueAsString(body);

        given(this.productService.create(any(CreateProductRequest.class))).willReturn(response);
        //when and then
        this.mockMvc.perform(post("/api/admin/products/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Create a product success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Iphone 16"));
    }

    @Test
    void create_EmptyName_ThrowBadRequestException() throws Exception {
        //given
        CreateProductRequest body = new CreateProductRequest(
                "",
                new BigDecimal("300.0"),
                "A new phone is...",
                1,
                "Apple",
                "/#"
        );


        String json = objectMapper.writeValueAsString(body);

        given(this.productService.create(any(CreateProductRequest.class))).willThrow(DataIntegrityViolationException.class);
        //when and then
        this.mockMvc.perform(post("/api/admin/products/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("name: name cannot be empty"))
                .andExpect(jsonPath("$.data").value("/api/admin/products/"));
    }

    @Test
    void updateProduct_ValidInput_ShouldReturnResponse() throws Exception {
        UpdateProductRequest body = UpdateProductRequest.builder()
                .name("Iphone 16 pro")
                .price(new BigDecimal(500.0))
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

        String json = objectMapper.writeValueAsString(body);

        given(this.productService.updateProduct(eq(1L), any(UpdateProductRequest.class))).willReturn(response);
        //when and then
        this.mockMvc.perform(patch("/api/admin/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Update a product success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Iphone 16 pro"))
                .andExpect(jsonPath("$.data.price").value(500));
    }

    @Test
    void updateProduct_NotFoundId_ThrowNotFoundException() throws Exception {
        UpdateProductRequest body = UpdateProductRequest.builder()
                .name("Iphone 16 pro")
                .price(new BigDecimal(500.0))
                .build();

        String json = objectMapper.writeValueAsString(body);

        given(this.productService.updateProduct(eq(1L), any(UpdateProductRequest.class)))
                .willThrow(new NotFoundException("Resource not found"));
        //when and then
        this.mockMvc.perform(patch("/api/admin/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Resource not found"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void updateProduct_EmptyName_ThrowBadRequestException() throws Exception {
        UpdateProductRequest body = UpdateProductRequest.builder()
                .name("")
                .price(new BigDecimal(500.0))
                .build();

        String json = objectMapper.writeValueAsString(body);

        given(this.productService.updateProduct(eq(1L), any(UpdateProductRequest.class))).willThrow(DataIntegrityViolationException.class);
        //when and then
        this.mockMvc.perform(patch("/api/admin/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("name: name cannot be empty"))
                .andExpect(jsonPath("$.data").value("/api/admin/products/1"));

    }

    @Test
    void updateStock_ValidStock_ShouldReturnResponse() throws Exception {
        StockProductDto body = new StockProductDto(10);

        StockProductDto response = new StockProductDto(60);

        String json = objectMapper.writeValueAsString(body);

        given(this.productService.updateStockById(eq(1L), any(StockProductDto.class)))
                .willReturn(response);
        //when and then
        this.mockMvc.perform(patch("/api/admin/products/1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Update stock of product success"))
                .andExpect(jsonPath("$.data.stock").value(60));

    }

    @Test
    void remove_ExistingId_ShouldReturnResponse() throws Exception {
        //given
        doNothing().when(productService).remove(1L);

        //when and then
        this.mockMvc.perform(delete("/api/admin/products/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    void remove_NotFoundId_ThrowNotFoundException() throws Exception {
        //given
        doThrow(new NotFoundException("Resource not found")).when(productService).remove(1L);

        //when and then
        this.mockMvc.perform(delete("/api/admin/products/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Resource not found"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void getStockInfo_ExistingId_ShouldReturnResponse() throws Exception {
        given(this.productService.getStockInfo(1L)).willReturn(new ProductRepository.StockView() {
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
        });
        //when and then
        this.mockMvc.perform(get("/api/admin/products/1/stock")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get stock info of product success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("test"))
                .andExpect(jsonPath("$.data.stock").value(50));
    }

    @Test
    void getStockInfo_NotFoundId_ThrowNotFoundException() throws Exception {
        given(this.productService.getStockInfo(1L)).willThrow(new NotFoundException("Resource not found"));
        //when and then
        this.mockMvc.perform(get("/api/admin/products/1/stock")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Resource not found"))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}