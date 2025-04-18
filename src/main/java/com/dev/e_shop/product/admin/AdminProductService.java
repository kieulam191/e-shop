package com.dev.e_shop.product;

import com.dev.e_shop.dto.PaginationResponse;
import com.dev.e_shop.exception.NotFoundException;
import com.dev.e_shop.product.dto.*;
import com.dev.e_shop.product.mapper.ProductMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final boolean DELETE = true;
    private final boolean NOT_DELETED = false;
    private final String PRODUCT_KEY = "products";
    private final String PAGINATION_KEY = "pagination";

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    public ProductResponse create(CreateProductRequest body) {
        Product product = productMapper.toProduct(body);

        Product savedProduct = productRepository.save(product);
        return productMapper.toProductResponse(savedProduct);
    }

    public ProductResponse updateProduct(long id, UpdateProductRequest body) {
        return productRepository.findById(id)
                .map(product -> {
                    if(body.getName() != null) product.setName(body.getName());
                    if(body.getBrand() != null) product.setBrand(body.getBrand());
                    if(body.getDescription() != null) product.setDescription(body.getDescription());
                    if(body.getPrice() != null) product.setPrice(body.getPrice());
                    if(body.getCategoryId() != 0) product.setCategoryId(body.getCategoryId());
                    if(body.getImgUrl() != null) product.setImgUrl(body.getImgUrl());

                    Product updatedProduct = productRepository.save(product);
                    return productMapper.toProductResponse(updatedProduct);
                })
                .orElseThrow(() -> new NotFoundException("Resource not found"));
    }

    public StockProductDto updateStockById(long id, StockProductDto body) {
         return productRepository.findById(id)
                .map(product -> {
                    product.setStock(body.getStock() + product.getStock());
                    Product updatedProduct = this.productRepository.save(product);

                    return new StockProductDto(updatedProduct.getStock());
                })
                .orElseThrow(() -> new NotFoundException("Resource not found"));
    }

    public void remove(long id) {
        productRepository.findById(1L)
                .map(product ->{
                    product.setDeleted(DELETE);

                    productRepository.save(product);
                    return product;
                })
                .orElseThrow(() -> new NotFoundException("Resource not found"));
    }

    public ProductRepository.StockView getStockInfo(long id) {
        return productRepository.findStockViewById(id)
                .orElseThrow(() -> new NotFoundException("Resource not found"));
    }
//
//    public Map<String, Object> getProductsByPagination(int page, int size) {
//        PageRequest pageRequest = PageRequest.of(page, size);
//
//        Page<Product> productPage = productRepository.findAll(pageRequest);
//
//        PaginationResponse paginationResponse = new PaginationResponse(
//                productPage.getNumber(),
//                productPage.getTotalPages(),
//                productPage.getTotalElements(),
//                productPage.getSize()
//        );
//
//        Map<String, Object> data = new HashMap<>();
//        data.put(PRODUCT_KEY, productPage.getContent()
//                .stream()
//                .map(product -> productMapper.toProductResponse(product))
//                .collect(Collectors.toList())
//        );
//        data.put(PAGINATION_KEY, paginationResponse);
//
//        return data;
//
//    }
//
//    public ProductResponse getById(long id) {
//        Product product = this.productRepository.findById(id)
//                .orElseThrow(() -> new NotFoundException("Resource not found"));
//
//        return productMapper.toProductResponse(product);
//    }
//
//    public ProductResponse getProductByName(String name) {
//        Product product = this.productRepository.findByNameIgnoreCase(name)
//                .orElseThrow(() -> new NotFoundException("Resource not found"));
//
//        return productMapper.toProductResponse(product);
//    }
//
//    public Map<String, Object> getProductContainByName(String name) {
//        int page = 0;
//        int size = 2;
//        Page<Product> productPage = this.productRepository.findByNameContainingIgnoreCase(name, PageRequest.of(page, size));
//
//        Map<String, Object> response = new HashMap<>();
//        response.put(PRODUCT_KEY, productPage.getContent()
//                .stream()
//                .map(product -> productMapper.toProductResponse(product))
//                .collect(Collectors.toList())
//        );
//        response.put("pagination", new PaginationResponse(
//                productPage.getNumber(),
//                productPage.getTotalPages(),
//                productPage.getTotalElements(),
//                productPage.getSize()
//        ));
//
//        return response;
//    }
}
