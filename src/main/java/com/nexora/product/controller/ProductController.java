package com.nexora.product.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexora.product.dto.ProductRequest;
import com.nexora.product.dto.ProductResponse;
import com.nexora.product.dto.ProductStatusRequest;
import com.nexora.product.service.ProductService;
import com.nexora.response.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(
            ProductService productService) {

        this.productService = productService;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>>
            createProduct(
            @Valid @RequestBody ProductRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        ProductResponse product =
                productService.createProduct(
                        request,
                        email);

        ApiResponse<ProductResponse> response =
                new ApiResponse<>(
                        true,
                        "Product created successfully",
                        product);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>>
            getAllProducts(
            Authentication authentication) {

        String email = authentication.getName();

        List<ProductResponse> products =
                productService.getAllProducts(email);

        ApiResponse<List<ProductResponse>> response =
                new ApiResponse<>(
                        true,
                        "Products fetched successfully",
                        products);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>>
            getProductById(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();

        ProductResponse product =
                productService.getProductById(
                        id,
                        email);

        ApiResponse<ProductResponse> response =
                new ApiResponse<>(
                        true,
                        "Product fetched successfully",
                        product);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>>
            updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        ProductResponse product =
                productService.updateProduct(
                        id,
                        request,
                        email);

        ApiResponse<ProductResponse> response =
                new ApiResponse<>(
                        true,
                        "Product updated successfully",
                        product);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    // UPDATE STATUS
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ProductResponse>>
            updateProductStatus(
            @PathVariable Long id,
            @RequestBody ProductStatusRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        ProductResponse product =
                productService.updateProductStatus(
                        id,
                        request,
                        email);

        ApiResponse<ProductResponse> response =
                new ApiResponse<>(
                        true,
                        "Product status updated successfully",
                        product);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>>
            deleteProduct(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();

        productService.deleteProduct(
                id,
                email);

        ApiResponse<Object> response =
                new ApiResponse<>(
                        true,
                        "Product deleted successfully",
                        null);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}