package com.dev.e_shop.product;

import com.dev.e_shop.dto.ApiResponse;
import com.dev.e_shop.dto.PaginationResponse;
import com.dev.e_shop.product.dto.ProductResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public/products")
public class UserProductController {
    private final ProductService productService;

    public UserProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductsByPagination(
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "1", required = false) int size
    ) {

        Map<String, Object> data = productService.getProductsByPagination(page, size);

        return ResponseEntity.status(200)
                .body(new ApiResponse<>(
                        200,
                        "Get products success",
                        data
                ));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductByName(@RequestParam String name) {
        ProductResponse product = productService.getProductByName(name);

        return ResponseEntity.status(200)
                .body(new ApiResponse<>(
                        200,
                        "Get product success",
                        product
                ));
    }
    @GetMapping("/test/search")
    public ResponseEntity<ApiResponse<Map<String, Object>>>  getProductContainByName(@RequestParam String name) {
        Map<String, Object> product = productService.getProductContainByName(name);

        return ResponseEntity.status(200)
                .body(new ApiResponse<>(
                        200,
                        "Get products success",
                        product
                ));
    }



    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable long id) {
        ProductResponse product = productService.getById(id);

        return ResponseEntity.status(200)
                .body(new ApiResponse<>(
                        200,
                        "Get product success",
                        product
                ));
    }


}
