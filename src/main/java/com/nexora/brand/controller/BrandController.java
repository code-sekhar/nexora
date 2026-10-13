package com.nexora.brand.controller;

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

import com.nexora.brand.dto.BrandRequest;
import com.nexora.brand.dto.BrandResponse;
import com.nexora.brand.dto.BrandStatusRequest;
import com.nexora.brand.service.BrandService;
import com.nexora.response.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/brands")
public class BrandController {

    private final BrandService brandService;

    public BrandController(
            BrandService brandService) {

        this.brandService = brandService;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<ApiResponse<BrandResponse>>
            createBrand(
            @Valid @RequestBody BrandRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        BrandResponse brand =
                brandService.createBrand(
                        request,
                        email);

        ApiResponse<BrandResponse> response =
                new ApiResponse<>(
                        true,
                        "Brand created successfully",
                        brand);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<ApiResponse<List<BrandResponse>>>
            getAllBrands(
            Authentication authentication) {

        String email = authentication.getName();

        List<BrandResponse> brands =
                brandService.getAllBrands(email);

        ApiResponse<List<BrandResponse>> response =
                new ApiResponse<>(
                        true,
                        "Brands fetched successfully",
                        brands);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandResponse>>
            getBrandById(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();

        BrandResponse brand =
                brandService.getBrandById(
                        id,
                        email);

        ApiResponse<BrandResponse> response =
                new ApiResponse<>(
                        true,
                        "Brand fetched successfully",
                        brand);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandResponse>>
            updateBrand(
            @PathVariable Long id,
            @Valid @RequestBody BrandRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        BrandResponse brand =
                brandService.updateBrand(
                        id,
                        request,
                        email);

        ApiResponse<BrandResponse> response =
                new ApiResponse<>(
                        true,
                        "Brand updated successfully",
                        brand);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    // UPDATE STATUS
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<BrandResponse>>
            updateBrandStatus(
            @PathVariable Long id,
            @RequestBody BrandStatusRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        BrandResponse brand =
                brandService.updateBrandStatus(
                        id,
                        request,
                        email);

        ApiResponse<BrandResponse> response =
                new ApiResponse<>(
                        true,
                        "Brand status updated successfully",
                        brand);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>>
            deleteBrand(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();

        brandService.deleteBrand(
                id,
                email);

        ApiResponse<Object> response =
                new ApiResponse<>(
                        true,
                        "Brand deleted successfully",
                        null);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}