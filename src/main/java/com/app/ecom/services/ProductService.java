package com.app.ecom.services;

import com.app.ecom.dto.ProductRequest;
import com.app.ecom.dto.ProductResponse;
import com.app.ecom.model.Product;
import com.app.ecom.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse addProduct(ProductRequest productRequest) {
        Product product = new Product();
        updateProductFromRequest(product, productRequest);
        Product savedProduct = productRepository.save(product);
        return mapToProductResponse(savedProduct);
    }

    public Optional<ProductResponse> updateProduct(Long id, ProductRequest productRequest) {

        return productRepository.findById(id)
                .map(existing -> {
                    updateProductFromRequest(existing, productRequest);
                    Product savedProduct = productRepository.save(existing);
                    return mapToProductResponse(savedProduct);
                });
    }

    public List<ProductResponse> fetchAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToProductResponse)
                .toList();
    }

    public Optional<ProductResponse> fetchProduct(Long id) {
        return productRepository.findById(id)
                .map(this::mapToProductResponse);
    }

    public ResponseEntity<ProductResponse> setInactive(Long id) {
        return productRepository.findById(id)
                .map(existing -> {
                    existing.setActive(false);
                    productRepository.save(existing);
                    return ResponseEntity.ok(mapToProductResponse(existing));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    //DTOs
    private ProductResponse mapToProductResponse(Product savedProduct) {

        return ProductResponse.builder()
                .id(savedProduct.getId())
                .name(savedProduct.getName())
                .description(savedProduct.getDescription())
                .price(savedProduct.getPrice())
                .quantity(savedProduct.getQuantity())
                .category(savedProduct.getCategory())
                .imageUrl(savedProduct.getImageUrl())
                .active(savedProduct.getActive())
                .build();
    }

    private void updateProductFromRequest(Product product, ProductRequest productRequest) {
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setQuantity(productRequest.getQuantity());
        product.setCategory(productRequest.getCategory());
        product.setImageUrl(productRequest.getImageUrl());
    }

}
