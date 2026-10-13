package com.nexora.expense.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.nexora.business.entity.Business;
import com.nexora.business.service.BusinessService;
import com.nexora.expense.dto.ExpensePaymentRequest;
import com.nexora.expense.dto.ExpensePaymentResponse;
import com.nexora.expense.service.ExpensePaymentService;
import com.nexora.response.ApiResponse;

@RestController
@RequestMapping("/api/expense-payments")
public class ExpensePaymentController {

    private final ExpensePaymentService paymentService;
    private final BusinessService businessService;

    public ExpensePaymentController(
            ExpensePaymentService paymentService,
            BusinessService businessService) {

        this.paymentService = paymentService;
        this.businessService = businessService;
    }

    /*
     * =========================================================
     * CREATE PAYMENT
     * =========================================================
     */

    @PostMapping
    public ResponseEntity<ApiResponse<ExpensePaymentResponse>>
    createPayment(
            @RequestBody ExpensePaymentRequest request,
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        ExpensePaymentResponse response =
                paymentService.createPayment(
                        business,
                        request
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Expense payment created successfully",
                        response
                )
        );
    }

    /*
     * =========================================================
     * GET ALL
     * =========================================================
     */

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpensePaymentResponse>>>
    getAllPayments(
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Expense payments fetched successfully",
                        paymentService.getAllPayments(business)
                )
        );
    }

    /*
     * =========================================================
     * GET BY ID
     * =========================================================
     */

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpensePaymentResponse>>
    getPaymentById(
            @PathVariable Long id,
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Expense payment fetched successfully",
                        paymentService.getPaymentById(
                                business,
                                id
                        )
                )
        );
    }

    /*
     * =========================================================
     * GET BY EXPENSE
     * =========================================================
     */

    @GetMapping("/expense/{expenseId}")
    public ResponseEntity<ApiResponse<List<ExpensePaymentResponse>>>
    getPaymentsByExpense(
            @PathVariable Long expenseId,
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Expense payments fetched successfully",
                        paymentService.getPaymentsByExpense(
                                business,
                                expenseId
                        )
                )
        );
    }

    /*
     * =========================================================
     * DATE RANGE
     * =========================================================
     */

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<ExpensePaymentResponse>>>
    getPaymentsByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Expense payments fetched successfully",
                        paymentService.getPaymentsByDateRange(
                                business,
                                startDate,
                                endDate
                        )
                )
        );
    }

    /*
     * =========================================================
     * CURRENT USER BUSINESS
     * =========================================================
     */

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