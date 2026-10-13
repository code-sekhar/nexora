package com.nexora.expense.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.nexora.business.entity.Business;
import com.nexora.business.service.BusinessService;
import com.nexora.expense.dto.ExpenseRequest;
import com.nexora.expense.dto.ExpenseResponse;
import com.nexora.expense.service.ExpenseService;
import com.nexora.response.ApiResponse;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final BusinessService businessService;

    public ExpenseController(
            ExpenseService expenseService,
            BusinessService businessService) {

        this.expenseService = expenseService;
        this.businessService = businessService;
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody ExpenseRequest request,
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        ExpenseResponse response =
                expenseService.create(
                        business,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        new ApiResponse<>(
                                true,
                                "Expense created successfully",
                                response
                        )
                );
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        List<ExpenseResponse> response =
                expenseService.getAll(business);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Expenses fetched successfully",
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

        ExpenseResponse response =
                expenseService.getById(
                        business,
                        id
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Expense fetched successfully",
                        response
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody ExpenseRequest request,
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        ExpenseResponse response =
                expenseService.update(
                        business,
                        id,
                        request
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Expense updated successfully",
                        response
                )
        );
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(
            @PathVariable Long id,
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        ExpenseResponse response =
                expenseService.cancel(
                        business,
                        id
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Expense cancelled successfully",
                        response
                )
        );
    }

    @GetMapping("/date-range")
    public ResponseEntity<?> getByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        List<ExpenseResponse> response =
                expenseService.getByDateRange(
                        business,
                        startDate,
                        endDate
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Expenses fetched successfully",
                        response
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