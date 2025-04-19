package com.dev.e_shop.product.publics;

import com.dev.e_shop.dto.PaginationResponse;
import com.dev.e_shop.exception.NotFoundException;
import com.dev.e_shop.product.Product;
import com.dev.e_shop.product.ProductRepository;
import com.dev.e_shop.product.dto.ProductPreviewResponse;
import com.dev.e_shop.product.dto.ProductResponse;
import com.dev.e_shop.product.mapper.ProductMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PublicProductService {
    private final String PRODUCT_KEY = "products";
    private final String PAGINATION_KEY = "pagination";

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public PublicProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    public Map<String, Object> getProductsByPagination(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);

        Page<Product> productPage = productRepository.findAll(pageRequest);

        return createDataByPagination(productPage);
    }

    public ProductResponse getProductDetailById(long id) {
        Product product = this.productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Resource not found"));

        return productMapper.toProductResponse(product);
    }


    public Map<String, Object> getProductContainByName(String name, int page, int size) {
        Page<Product> productPage = this.productRepository.findByNameContainingIgnoreCase(name, PageRequest.of(page, size));

        return createDataByPagination(productPage);
    }

    private Map<String, Object> createDataByPagination(Page<Product> productPage) {
        Map<String, Object> data = new HashMap<>();
        data.put(PRODUCT_KEY, productPage.getContent()
                .stream()
                .map(product -> productMapper.toProductPreviewResponse((product)))
                .collect(Collectors.toList())
        );
        data.put(PAGINATION_KEY, new PaginationResponse(
                productPage.getNumber(),
                productPage.getTotalPages(),
                productPage.getTotalElements(),
                productPage.getSize()
        ));

        return data;
    }
}
