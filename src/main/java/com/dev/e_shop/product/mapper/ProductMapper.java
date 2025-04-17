package com.dev.e_shop.product.mapper;

import com.dev.e_shop.product.Product;
import com.dev.e_shop.product.dto.CreateProductRequest;
import com.dev.e_shop.product.dto.ProductResponse;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface ProductMapper {

    ProductResponse toProductResponse(Product entity);

    Product toProduct(ProductResponse dto);

    Product toProduct(CreateProductRequest request);
}
