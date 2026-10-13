package com.nexora.account.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.nexora.account.dto.AccountTransactionRequest;
import com.nexora.account.dto.AccountTransactionResponse;
import com.nexora.account.service.AccountTransactionService;
import com.nexora.business.entity.Business;
import com.nexora.business.service.BusinessService;
import com.nexora.response.ApiResponse;

@RestController
@RequestMapping("/api/account-transactions")
public class AccountTransactionController {

    private final AccountTransactionService transactionService;
    private final BusinessService businessService;

    public AccountTransactionController(
            AccountTransactionService transactionService,
            BusinessService businessService) {

        this.transactionService = transactionService;
        this.businessService = businessService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccountTransactionResponse>>
    createTransaction(
            @RequestBody AccountTransactionRequest request,
            Authentication authentication) {

        Business business = getCurrentUserBusiness(authentication);

        AccountTransactionResponse response =
                transactionService.createTransaction(
                        request,
                        business
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Account transaction created successfully",
                        response
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountTransactionResponse>>>
    getAllTransactions(
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        List<AccountTransactionResponse> response =
                transactionService.getAllTransactions(business);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Account transactions fetched successfully",
                        response
                )
        );
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<ApiResponse<List<AccountTransactionResponse>>>
    getAccountTransactions(
            @PathVariable Long accountId,
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        List<AccountTransactionResponse> response =
                transactionService.getAccountTransactions(
                        accountId,
                        business
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Account transactions fetched successfully",
                        response
                )
        );
    }

    @GetMapping("/account/{accountId}/date-range")
    public ResponseEntity<ApiResponse<List<AccountTransactionResponse>>>
    getAccountTransactionsByDateRange(
            @PathVariable Long accountId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        LocalDateTime start =
                startDate.atStartOfDay();

        LocalDateTime end =
                endDate.atTime(LocalTime.MAX);

        List<AccountTransactionResponse> response =
                transactionService
                        .getAccountTransactionsByDateRange(
                                accountId,
                                start,
                                end,
                                business
                        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Account transactions fetched successfully",
                        response
                )
        );
    }

    private Business getCurrentUserBusiness(
            Authentication authentication) {

        String email = authentication.getName();

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