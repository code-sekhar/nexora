package com.nexora.account.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.nexora.account.dto.AccountRequest;
import com.nexora.account.dto.AccountResponse;
import com.nexora.account.service.AccountService;
import com.nexora.business.entity.Business;
import com.nexora.business.service.BusinessService;
import com.nexora.response.ApiResponse;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final BusinessService businessService;

    public AccountController(
            AccountService accountService,
            BusinessService businessService) {

        this.accountService = accountService;
        this.businessService = businessService;
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody AccountRequest request,
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        AccountResponse response =
                accountService.create(
                        business,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        new ApiResponse<>(
                                true,
                                "Account created successfully",
                                response
                        )
                );
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        List<AccountResponse> response =
                accountService.getAll(
                        business
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Accounts fetched successfully",
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

        AccountResponse response =
                accountService.getById(
                        business,
                        id
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Account fetched successfully",
                        response
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody AccountRequest request,
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        AccountResponse response =
                accountService.update(
                        business,
                        id,
                        request
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Account updated successfully",
                        response
                )
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivate(
            @PathVariable Long id,
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        AccountResponse response =
                accountService.deactivate(
                        business,
                        id
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Account deactivated successfully",
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
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<AccountResponse>> activateAccount(
            @PathVariable Long id,
            Authentication authentication) {

        Business business = getCurrentUserBusiness(authentication);

        AccountResponse response =
                accountService.activateAccount(
                        id,
                        business
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Account activated successfully",
                        response
                )
        );
    }
}