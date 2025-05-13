package com.dev.e_shop.product.admin;

import com.dev.e_shop.exception.custom.NotFoundException;
import com.dev.e_shop.product.Product;
import com.dev.e_shop.product.ProductRepository;
import com.dev.e_shop.product.dto.*;
import com.dev.e_shop.product.mapper.ProductMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminProductService {
    private final boolean DELETE = true;
    private final boolean NOT_DELETED = false;

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public AdminProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse create(CreateProductRequest body) {
        Product product = productMapper.toProduct(body);

        Product savedProduct = productRepository.save(product);
        return productMapper.toProductResponse(savedProduct);
    }

    @Caching(evict = {
            @CacheEvict(value = "product", key = "#id"),
            @CacheEvict(value = "products", allEntries = true)
    })
    public ProductResponse updateProduct(long id, UpdateProductRequest body) {
        return productRepository.findById(id)
                .map(product -> {
                    if(body.getName() != null) product.setName(body.getName());
                    if(body.getBrand() != null) product.setBrand(body.getBrand());
                    if(body.getDescription() != null) product.setDescription(body.getDescription());
                    if(body.getPrice() != null) product.setPrice(body.getPrice());
                    if(body.getCategoryId() > 0) product.setCategoryId(body.getCategoryId());
                    if(body.getImgUrl() != null) product.setImgUrl(body.getImgUrl());

                    Product updatedProduct = productRepository.save(product);
                    return productMapper.toProductResponse(updatedProduct);
                })
                .orElseThrow(() -> createNotFoundException(id));
    }

    public StockProductDto updateStockById(long id, StockProductDto body) {
         return productRepository.findById(id)
                .map(product -> {
                    product.setStock(body.getStock() + product.getStock());
                    Product updatedProduct = this.productRepository.save(product);

                    return new StockProductDto(updatedProduct.getStock());
                })
                .orElseThrow(() -> createNotFoundException(id));
    }

    @Caching(evict = {
            @CacheEvict(value = "product", key = "#id"),
            @CacheEvict(value = "products", allEntries = true)
    })
    public void remove(long id) {
        productRepository.findById(1L)
                .map(product ->{
                    product.setDeleted(DELETE);

                    productRepository.save(product);
                    return product;
                })
                .orElseThrow(() -> createNotFoundException(id));
    }

    public ProductRepository.StockView getStockInfo(long id) {
        return productRepository.findStockViewById(id)
                .orElseThrow(() -> createNotFoundException(id));
    }

    private NotFoundException createNotFoundException(long id) {
        return new NotFoundException(String.format("Product with ID %d not found", id));
    }
}
