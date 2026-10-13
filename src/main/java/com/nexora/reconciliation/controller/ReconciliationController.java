package com.nexora.reconciliation.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.nexora.business.entity.Business;
import com.nexora.business.service.BusinessService;
import com.nexora.reconciliation.dto.AutoMatchCandidateResponse;
import com.nexora.reconciliation.dto.AutoMatchRequest;
import com.nexora.reconciliation.dto.ReconciliationRequest;
import com.nexora.reconciliation.dto.ReconciliationResponse;
import com.nexora.reconciliation.service.AutoMatchService;
import com.nexora.reconciliation.service.ReconciliationService;
import com.nexora.response.ApiResponse;

@RestController
@RequestMapping("/api/reconciliations")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;
    private final BusinessService businessService;
    private final AutoMatchService autoMatchService;

    public ReconciliationController(
            ReconciliationService reconciliationService,
            BusinessService businessService,
            AutoMatchService autoMatchService) {

        this.reconciliationService =
                reconciliationService;

        this.businessService =
                businessService;
        
        this.autoMatchService = autoMatchService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReconciliationResponse>>
    create(
            @RequestBody ReconciliationRequest request,
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        ReconciliationResponse response =
                reconciliationService.createReconciliation(
                        request,
                        business
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        new ApiResponse<>(
                                true,
                                "Reconciliation created successfully",
                                response
                        )
                );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReconciliationResponse>>>
    getAll(
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Reconciliations fetched successfully",
                        reconciliationService.getAll(business)
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReconciliationResponse>>
    getById(
            @PathVariable Long id,
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Reconciliation fetched successfully",
                        reconciliationService.getById(
                                id,
                                business
                        )
                )
        );
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<ApiResponse<List<ReconciliationResponse>>>
    getByAccount(
            @PathVariable Long accountId,
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Account reconciliations fetched successfully",
                        reconciliationService.getByAccount(
                                accountId,
                                business
                        )
                )
        );
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<ReconciliationResponse>>>
    getByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Reconciliations fetched successfully",
                        reconciliationService.getByDateRange(
                                startDate,
                                endDate,
                                business
                        )
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
                        ));
    }
    
    @PostMapping("/auto-match")
    public ResponseEntity<
            ApiResponse<List<AutoMatchCandidateResponse>>>
    autoMatch(
            @RequestBody AutoMatchRequest request,
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Auto-match candidates fetched successfully",
                        autoMatchService.findMatches(
                                request,
                                business
                        )
                )
        );
    }
}