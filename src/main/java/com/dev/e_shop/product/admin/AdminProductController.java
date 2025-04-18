package com.dev.e_shop.product;

import com.dev.e_shop.dto.ApiResponse;
import com.dev.e_shop.product.dto.CreateProductRequest;
import com.dev.e_shop.product.dto.ProductResponse;
import com.dev.e_shop.product.dto.UpdateProductRequest;
import com.dev.e_shop.product.dto.StockProductDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {
    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }


    @GetMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<ProductRepository.StockView>> getStockInfo(@PathVariable long id) {
        ProductRepository.StockView response = productService.getStockInfo(id);

        return ResponseEntity.status(200)
                .body(new ApiResponse<>(200, "Get stock info of product success", response));
    }


    @PostMapping("/")
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @Valid @RequestBody CreateProductRequest body) {
        ProductResponse response = productService.create(body);

        return ResponseEntity.status(201)
                .body(new ApiResponse<>(201, "Create a product success", response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable int id,
            @Valid @RequestBody UpdateProductRequest body) {
        ProductResponse response = productService.updateProduct(id, body);

        return ResponseEntity.status(200)
                .body(new ApiResponse<>(200, "Update a product success", response));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<StockProductDto>> updateStock(
            @PathVariable int id,
            @Valid @RequestBody StockProductDto body) {
        StockProductDto response = productService.updateStockById(id, body);

        return ResponseEntity.status(200).body(
                new ApiResponse<>(200, "Update stock of product success", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> remove(
            @PathVariable int id) {
        productService.remove(id);

        return ResponseEntity.status(204).build();
    }
}
