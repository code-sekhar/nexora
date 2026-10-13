package com.nexora.category.controller;

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

import com.nexora.category.dto.CategoryRequest;
import com.nexora.category.dto.CategoryResponse;
import com.nexora.category.dto.CategoryStatusRequest;
import com.nexora.category.service.CategoryService;
import com.nexora.response.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(
            CategoryService categoryService) {

        this.categoryService = categoryService;
    }


    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>>
            createCategory(

            @Valid @RequestBody CategoryRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        CategoryResponse category =
                categoryService.createCategory(
                        request,
                        email);

        ApiResponse<CategoryResponse> response =
                new ApiResponse<>(
                        true,
                        "Category created successfully",
                        category);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>>
            getAllCategories(
                    Authentication authentication) {

        String email = authentication.getName();

        List<CategoryResponse> categories =
                categoryService.getAllCategories(email);

        ApiResponse<List<CategoryResponse>> response =
                new ApiResponse<>(
                        true,
                        "Categories fetched successfully",
                        categories);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>>
            getCategoryById(

            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();

        CategoryResponse category =
                categoryService.getCategoryById(
                        id,
                        email);

        ApiResponse<CategoryResponse> response =
                new ApiResponse<>(
                        true,
                        "Category fetched successfully",
                        category);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>>
            updateCategory(

            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        CategoryResponse category =
                categoryService.updateCategory(
                        id,
                        request,
                        email);

        ApiResponse<CategoryResponse> response =
                new ApiResponse<>(
                        true,
                        "Category updated successfully",
                        category);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>>
            deleteCategory(

            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();

        categoryService.deleteCategory(
                id,
                email);

        ApiResponse<Object> response =
                new ApiResponse<>(
                        true,
                        "Category deleted successfully",
                        null);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<CategoryResponse>>
            updateCategoryStatus(
            @PathVariable Long id,
            @RequestBody CategoryStatusRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        CategoryResponse category =
                categoryService.updateCategoryStatus(
                        id,
                        request,
                        email);

        ApiResponse<CategoryResponse> response =
                new ApiResponse<>(
                        true,
                        "Category status updated successfully",
                        category);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}