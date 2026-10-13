package com.nexora.reconciliation.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.nexora.business.entity.Business;
import com.nexora.business.service.BusinessService;
import com.nexora.reconciliation.dto.ReconciliationItemRequest;
import com.nexora.reconciliation.dto.ReconciliationItemResponse;
import com.nexora.reconciliation.service.ReconciliationItemService;
import com.nexora.response.ApiResponse;

@RestController
@RequestMapping("/api/reconciliation-items")
public class ReconciliationItemController {

    private final ReconciliationItemService itemService;
    private final BusinessService businessService;

    public ReconciliationItemController(
            ReconciliationItemService itemService,
            BusinessService businessService) {

        this.itemService = itemService;
        this.businessService = businessService;
    }

    @PostMapping("/reconciliation/{reconciliationId}")
    public ResponseEntity<
            ApiResponse<ReconciliationItemResponse>>
    create(
            @PathVariable Long reconciliationId,
            @RequestBody ReconciliationItemRequest request,
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        ReconciliationItemResponse response =
                itemService.createItem(
                        reconciliationId,
                        request,
                        business
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        new ApiResponse<>(
                                true,
                                "Reconciliation item created successfully",
                                response
                        )
                );
    }

    @GetMapping("/reconciliation/{reconciliationId}")
    public ResponseEntity<
            ApiResponse<List<ReconciliationItemResponse>>>
    getByReconciliation(
            @PathVariable Long reconciliationId,
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Reconciliation items fetched successfully",
                        itemService.getByReconciliation(
                                reconciliationId,
                                business
                        )
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<
            ApiResponse<ReconciliationItemResponse>>
    getById(
            @PathVariable Long id,
            Authentication authentication) {

        Business business =
                getCurrentUserBusiness(authentication);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Reconciliation item fetched successfully",
                        itemService.getById(
                                id,
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
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(
            @PathVariable Long id,
            Authentication authentication) {

        Business business = getCurrentUserBusiness(authentication);

        itemService.deleteItem(id, business);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Reconciliation item unmatched successfully",
                        null
                )
        );
    }
}