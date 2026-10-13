package com.nexora.purchase.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.nexora.purchase.dto.PurchaseRequest;
import com.nexora.purchase.dto.PurchaseResponse;
import com.nexora.purchase.service.PurchaseService;
import com.nexora.response.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(
            PurchaseService purchaseService) {

        this.purchaseService = purchaseService;
    }


    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseResponse>>
    createPurchase(
            @Valid @RequestBody PurchaseRequest request,
            Authentication authentication) {

        PurchaseResponse response =
            purchaseService.createPurchase(
                request,
                authentication.getName()
            );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                    new ApiResponse<>(
                        true,
                        "Purchase created successfully",
                        response
                    )
                );
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<PurchaseResponse>>>
    getAllPurchases(
            Authentication authentication) {

        List<PurchaseResponse> response =
            purchaseService.getAllPurchases(
                authentication.getName()
            );

        return ResponseEntity.ok(
            new ApiResponse<>(
                true,
                "Purchases fetched successfully",
                response
            )
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseResponse>>
    getPurchaseById(
            @PathVariable Long id,
            Authentication authentication) {

        PurchaseResponse response =
            purchaseService.getPurchaseById(
                id,
                authentication.getName()
            );

        return ResponseEntity.ok(
            new ApiResponse<>(
                true,
                "Purchase fetched successfully",
                response
            )
        );
    }
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<PurchaseResponse>> cancelPurchase(
            @PathVariable Long id,
            Authentication authentication
    ) {

        PurchaseResponse response =
                purchaseService.cancelPurchase(
                        id,
                        authentication.getName()
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Purchase cancelled successfully",
                        response
                )
        );
    }
}