package com.dev.e_shop.cart;

import com.dev.e_shop.cart.dto.AddItemRequest;
import com.dev.e_shop.cart.dto.CartDto;
import com.dev.e_shop.cart.dto.CartResponse;
import com.dev.e_shop.cart.dto.UpdateItemRequest;
import com.dev.e_shop.exception.custom.NotFoundException;
import com.dev.e_shop.user.User;
import com.dev.e_shop.user.UserDetail;
import com.dev.e_shop.user.profile.Profile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "user", roles = "USER")
class UserCartControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserCartService userCartService;

    @Autowired
    ObjectMapper objectMapper;

    UserDetail userDetail;

    private CartResponse cartResponse;

    @BeforeEach
    void setUp() {
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUserId(1L);

        User user = new User();
        user.setEmail("test@gmail.com");
        user.setId(1L);

        userDetail = new UserDetail(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetail, null,
                userDetail.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Set<CartDto> carts = new HashSet<>();
        CartDto item1 = new CartDto(1,1,1);
        CartDto item2 = new CartDto(2,1,2);
        carts.add(item1);
        carts.add(item2);

        BigDecimal mockTotalPrice = new BigDecimal("15");

        cartResponse = new CartResponse(carts, mockTotalPrice);
    }

    @Test
    void getCartByUserId_withValidInput_returnsCartResponse() throws Exception {
        //given
        Set<CartDto> carts = new HashSet<>();
        CartDto item1 = new CartDto(1,1,1);
        CartDto item2 = new CartDto(2,1,2);
        carts.add(item1);
        carts.add(item2);

        BigDecimal mockTotal = new BigDecimal("15");
        CartResponse response = new CartResponse(carts, mockTotal);

        given(userCartService.getCartByUserId(1L)).willReturn(response);

        //when and then
        this.mockMvc.perform(get("/api/user/cart/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get the cart success"))
                .andExpect(jsonPath("$.data.size()", Matchers.equalTo(2)));
    }

    @Test
    void addCartItem_withValidInput_returns201() throws Exception {
        //given
        AddItemRequest body = new AddItemRequest(1L);
        String json = objectMapper.writeValueAsString(body);

        given(this.userCartService.addCartItem(body, userDetail))
                .willReturn(cartResponse);


        this.mockMvc.perform(post("/api/user/cart/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Add the item to cart success"))
                .andExpect(jsonPath("$.data.carts.size()", Matchers.equalTo(2)));
    }

    @Test
    void addCartItem_withNotFoundProductId_throwsNotFoundException() throws Exception {
        //given
        AddItemRequest body = new AddItemRequest(1L);
        String json = objectMapper.writeValueAsString(body);

        willThrow(new NotFoundException("Product with ID 1 not found")).given(this.userCartService)
                .addCartItem(body, userDetail);


        this.mockMvc.perform(post("/api/user/cart/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Resource not found"))
                .andExpect(jsonPath("$.errors[0]").value("Product with ID 1 not found"))
                .andExpect(jsonPath("$.path").value("/api/user/cart/me"));

    }

    @Test
    void addCartItem_withExistingCartItem_returnsCartResponse() throws Exception {
        //given
        CartDto item1 = new CartDto(1,1,2);
        CartResponse cartResponse = new CartResponse(Set.of(item1), BigDecimal.valueOf(15));

        AddItemRequest body = new AddItemRequest(1L);
        String json = objectMapper.writeValueAsString(body);

        given(this.userCartService.addCartItem(any(AddItemRequest.class), any(UserDetail.class)))
                .willReturn(cartResponse);


        this.mockMvc.perform(post("/api/user/cart/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Add the item to cart success"))
                .andExpect(jsonPath("$.data.carts.size()", Matchers.equalTo(1)))
                .andExpect(jsonPath("$.data.carts[0].quantity").value(2));
    }

    @Test
    void updateQuantityOfItem_withValidAmount_returns200() throws Exception {
        //given
        UpdateItemRequest body = new UpdateItemRequest(1,5);
        String json = objectMapper.writeValueAsString(body);

        CartDto updatedItem = new CartDto(1,1,5);
        CartResponse cartResponse = new CartResponse(Set.of(updatedItem), BigDecimal.valueOf(15));

        given(this.userCartService.updateQuantityOfItem(any(UpdateItemRequest.class), any(UserDetail.class)))
                .willReturn(cartResponse);

        //when and then
        this.mockMvc.perform(patch("/api/user/cart/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Update the item to cart success"))
                .andExpect(jsonPath("$.data.carts.size()", Matchers.equalTo(1)))
                .andExpect(jsonPath("$.data.carts[0].quantity").value(5));
    }

    @Test
    void updateQuantityOfItem_withAmountBelow1_returnsBadRequestException() throws Exception {
        //given
        UpdateItemRequest body = new UpdateItemRequest(1,0);
        String json = objectMapper.writeValueAsString(body);

        given(this.userCartService
                .updateQuantityOfItem(any(UpdateItemRequest.class), any(UserDetail.class)))
                .willReturn(cartResponse);

        //when and then
        this.mockMvc.perform(patch("/api/user/cart/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
                .andExpect(jsonPath("$.errors.amount").value("Amount must be greater than 0"))
                .andExpect(jsonPath("$.path").value("/api/user/cart/me"));
    }

    @Test
    void updateQuantityOfItem_withNotFoundCardItemId_returnsNotFoundException() throws Exception {
        //given
        UpdateItemRequest body = new UpdateItemRequest(1000,5);
        String json = objectMapper.writeValueAsString(body);


        doThrow(new NotFoundException("Item cart with ID 1000 not found in your cart"))
                .when(this.userCartService).updateQuantityOfItem(body, userDetail);

        //when and then
        this.mockMvc.perform(patch("/api/user/cart/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Resource not found"))
                .andExpect(jsonPath("$.errors[0]").value("Item cart with ID 1000 not found in your cart"))
                .andExpect(jsonPath("$.path").value("/api/user/cart/me"));
    }

    @Test
    void removeCartItem_withExistingCartItemId_returns200() throws Exception {
        //given
        CartDto cartItem2 = new CartDto(2,2,1);
        CartResponse cartResponse = new CartResponse(Set.of(cartItem2), BigDecimal.valueOf(15));

        given(this.userCartService
                .removeCartItem(1L, userDetail))
                .willReturn(cartResponse);

        //when and then
        this.mockMvc.perform(delete("/api/user/cart/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Delete the item to cart success"))
                .andExpect(jsonPath("$.data.carts.size()", Matchers.equalTo(1)))
                .andExpect(jsonPath("$.data.carts[0].id").value(2));
    }

    @Test
    void removeCartItem_withNotFoundCartItemId_returnsNotFoundException() throws Exception {
        //given
        doThrow(new NotFoundException("Item cart with ID 1 not found in your cart"))
                .when(this.userCartService).removeCartItem(1, userDetail);


        //when and then
        this.mockMvc.perform(delete("/api/user/cart/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Resource not found"))
                .andExpect(jsonPath("$.errors[0]").value("Item cart with ID 1 not found in your cart"))
                .andExpect(jsonPath("$.path").value("/api/user/cart/1"));
    }
}