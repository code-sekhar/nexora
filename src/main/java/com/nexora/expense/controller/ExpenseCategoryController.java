package com.nexora.expense.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.nexora.business.entity.Business;
import com.nexora.business.service.BusinessService;
import com.nexora.expense.dto.ExpenseCategoryRequest;
import com.nexora.expense.dto.ExpenseCategoryResponse;
import com.nexora.expense.service.ExpenseCategoryService;
import com.nexora.response.ApiResponse;

@RestController
@RequestMapping("/api/expense-categories")
public class ExpenseCategoryController {

    private final ExpenseCategoryService service;
    private final BusinessService businessService;

    public ExpenseCategoryController(
            ExpenseCategoryService service,
            BusinessService businessService) {

        this.service = service;
        this.businessService = businessService;
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody ExpenseCategoryRequest request,
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        ExpenseCategoryResponse response =
                service.create(
                        business,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        new ApiResponse<>(
                                true,
                                "Expense category created successfully",
                                response
                        )
                );
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        List<ExpenseCategoryResponse> response =
                service.getAll(business);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Expense categories fetched successfully",
                        response
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @PathVariable Long id,
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        ExpenseCategoryResponse response =
                service.getById(
                        business,
                        id
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Expense category fetched successfully",
                        response
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody ExpenseCategoryRequest request,
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        ExpenseCategoryResponse response =
                service.update(
                        business,
                        id,
                        request
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Expense category updated successfully",
                        response
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable Long id,
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        service.delete(
                business,
                id
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Expense category deactivated successfully",
                        null
                )
        );
    }

    private Business getCurrentUserBusiness(
            Authentication authentication) {

        String email =
                authentication.getName();

        return businessService
                .getAllBusinesses(email)
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(
                                "Business not assigned to user"
                        )
                );
    }
}