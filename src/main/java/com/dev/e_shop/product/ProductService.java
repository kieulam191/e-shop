package com.dev.e_shop.product;

import com.dev.e_shop.exception.NotFoundException;
import com.dev.e_shop.product.dto.*;
import com.dev.e_shop.product.mapper.ProductMapper;
import org.springframework.stereotype.Service;

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
}
