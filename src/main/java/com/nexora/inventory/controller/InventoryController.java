package com.nexora.inventory.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.nexora.inventory.dto.InventoryResponse;
import com.nexora.inventory.dto.StockAdjustmentRequest;
import com.nexora.inventory.dto.StockMovementResponse;
import com.nexora.inventory.service.InventoryService;
import com.nexora.response.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(
            InventoryService inventoryService) {

        this.inventoryService = inventoryService;
    }

    // GET ALL INVENTORY
    @GetMapping
    public ResponseEntity<
            ApiResponse<List<InventoryResponse>>> getAllInventory(
            Authentication authentication) {

        String email =
                authentication.getName();

        List<InventoryResponse> inventory =
                inventoryService.getAllInventory(email);

        ApiResponse<List<InventoryResponse>> response =
                new ApiResponse<>(
                        true,
                        "Inventory fetched successfully",
                        inventory
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    // GET PRODUCT INVENTORY
    @GetMapping("/{productId}")
    public ResponseEntity<
            ApiResponse<InventoryResponse>> getInventory(
            @PathVariable Long productId,
            Authentication authentication) {

        String email =
                authentication.getName();

        InventoryResponse inventory =
                inventoryService.getInventoryByProduct(
                        productId,
                        email
                );

        ApiResponse<InventoryResponse> response =
                new ApiResponse<>(
                        true,
                        "Inventory fetched successfully",
                        inventory
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    // ADJUST STOCK
    @PostMapping("/{productId}/adjust")
    public ResponseEntity<
            ApiResponse<InventoryResponse>> adjustStock(
            @PathVariable Long productId,
            @Valid @RequestBody
            StockAdjustmentRequest request,
            Authentication authentication) {

        String email =
                authentication.getName();

        InventoryResponse inventory =
                inventoryService.adjustStock(
                        productId,
                        request,
                        email
                );

        ApiResponse<InventoryResponse> response =
                new ApiResponse<>(
                        true,
                        "Stock adjusted successfully",
                        inventory
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    // STOCK MOVEMENTS
    @GetMapping("/{productId}/movements")
    public ResponseEntity<
            ApiResponse<List<StockMovementResponse>>>
            getStockMovements(
                    @PathVariable Long productId,
                    Authentication authentication) {

        String email =
                authentication.getName();

        List<StockMovementResponse> movements =
                inventoryService.getStockMovements(
                        productId,
                        email
                );

        ApiResponse<List<StockMovementResponse>> response =
                new ApiResponse<>(
                        true,
                        "Stock movements fetched successfully",
                        movements
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}