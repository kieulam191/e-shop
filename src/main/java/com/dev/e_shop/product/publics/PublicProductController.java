package com.dev.e_shop.product.publics;

import com.dev.e_shop.dto.ApiResponse;
import com.dev.e_shop.dto.PaginationDto;
import com.dev.e_shop.product.dto.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public/products")
public class PublicProductController {
    private final PublicProductService productService;

    public PublicProductController(PublicProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductsByPagination(
            @Valid PaginationDto requestParamsDto) {

        Map<String, Object> data = productService.getProductsByPagination(
                requestParamsDto.getPageInt(),
                requestParamsDto.getSizeInt());

        return ResponseEntity.status(200)
                .body(new ApiResponse<>(
                        200,
                        "Get products success",
                        data
                ));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductByName(
            @RequestParam String name,
            @Valid PaginationDto requestParamsDto ) {
        Map<String, Object> product = productService.getProductContainByName(
                name,
                requestParamsDto.getPageInt(),
                requestParamsDto.getSizeInt());

        return ResponseEntity.status(200)
                .body(new ApiResponse<>(
                        200,
                        "Get products success",
                        product
                ));
    }



    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable long id) {
        ProductResponse product = productService.getProductDetailById(id);

        return ResponseEntity.status(200)
                .body(new ApiResponse<>(
                        200,
                        "Get product success",
                        product
                ));
    }
}
