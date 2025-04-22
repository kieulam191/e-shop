package com.dev.e_shop.cart;

import com.dev.e_shop.cart.dto.AddItemRequest;
import com.dev.e_shop.cart.dto.CartResponse;
import com.dev.e_shop.cart.dto.UpdateItemRequest;
import com.dev.e_shop.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/cart")
public class UserCartController {
    private final UserCartService userCartService;

    public UserCartController(UserCartService userCartService) {
        this.userCartService = userCartService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getCartByUserId(@RequestParam("user-id") long userId) {
       CartResponse response = userCartService.getCartByUserId(userId);

        return ResponseEntity.status(200)
                .body(new ApiResponse(
                        200,
                        "Get the cart success",
                        response
                ));
    }

    @PostMapping("/")
    public ResponseEntity<ApiResponse> addCartItem(@Valid @RequestBody AddItemRequest body) {
        userCartService.addCartItem(body);

        return ResponseEntity.status(201)
                .body(new ApiResponse(
                        201,
                        "Add the item to cart success",
                        null
                ));
    }

    @PatchMapping("/")
    public ResponseEntity<ApiResponse> updateCartItem(@Valid @RequestBody UpdateItemRequest body) {
        userCartService.updateQuantityOfItem(body);

        return ResponseEntity.status(200)
                .body(new ApiResponse(
                        200,
                        "Update the item to cart success",
                        null
                ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> removeCartItem(@PathVariable long id) {
        userCartService.removeCartItem(id);

        return ResponseEntity.status(200)
                .body(new ApiResponse(
                        200,
                        "Delete the item to cart success",
                        null
                ));
    }


}
