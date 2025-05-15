package com.dev.e_shop.cart;

import com.dev.e_shop.cart.dto.AddItemRequest;
import com.dev.e_shop.cart.dto.CartResponse;
import com.dev.e_shop.cart.dto.UpdateItemRequest;
import com.dev.e_shop.dto.ApiResponse;
import com.dev.e_shop.user.UserDetail;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/cart")
public class UserCartController {
    private final UserCartService userCartService;

    public UserCartController(UserCartService userCartService) {
        this.userCartService = userCartService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getCartByUserId(@AuthenticationPrincipal UserDetail userDetail) {
       CartResponse response = userCartService.getCartByUserId(userDetail.getId());

        return ResponseEntity.status(200)
                .body(new ApiResponse(
                        200,
                        "Get the cart success",
                        response
                ));
    }

    @PostMapping("/me")
    public ResponseEntity<ApiResponse> addCartItem(
            @AuthenticationPrincipal UserDetail userDetail,
            @Valid @RequestBody AddItemRequest body) {
        var cart = userCartService.addCartItem(body, userDetail);

        return ResponseEntity.status(201)
                .body(new ApiResponse(
                        201,
                        "Add the item to cart success",
                        cart
                ));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse> updateCartItem(
            @AuthenticationPrincipal UserDetail userDetail,
            @Valid @RequestBody UpdateItemRequest body) {
        var cart = userCartService.updateQuantityOfItem(body, userDetail);

        return ResponseEntity.status(200)
                .body(new ApiResponse(
                        200,
                        "Update the item to cart success",
                        cart
                ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> removeCartItem(
            @AuthenticationPrincipal UserDetail userDetail,
            @PathVariable long id) {
        var cart = userCartService.removeCartItem(id, userDetail);

        return ResponseEntity.status(200)
                .body(new ApiResponse(
                        200,
                        "Delete the item to cart success",
                        cart
                ));
    }


}
