package com.nexora.supplier.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.nexora.response.ApiResponse;
import com.nexora.supplier.dto.SupplierRequest;
import com.nexora.supplier.dto.SupplierResponse;
import com.nexora.supplier.dto.SupplierStatusRequest;
import com.nexora.supplier.service.SupplierService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(
            SupplierService supplierService) {

        this.supplierService = supplierService;
    }


    @PostMapping
    public ResponseEntity<ApiResponse<SupplierResponse>>
    createSupplier(
            @Valid @RequestBody SupplierRequest request,
            Authentication authentication) {

        SupplierResponse response =
            supplierService.createSupplier(
                request,
                authentication.getName()
            );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                    new ApiResponse<>(
                        true,
                        "Supplier created successfully",
                        response
                    )
                );
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<SupplierResponse>>>
    getAllSuppliers(
            Authentication authentication) {

        List<SupplierResponse> response =
            supplierService.getAllSuppliers(
                authentication.getName()
            );

        return ResponseEntity.ok(
            new ApiResponse<>(
                true,
                "Suppliers fetched successfully",
                response
            )
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierResponse>>
    getSupplierById(
            @PathVariable Long id,
            Authentication authentication) {

        SupplierResponse response =
            supplierService.getSupplierById(
                id,
                authentication.getName()
            );

        return ResponseEntity.ok(
            new ApiResponse<>(
                true,
                "Supplier fetched successfully",
                response
            )
        );
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierResponse>>
    updateSupplier(
            @PathVariable Long id,
            @Valid @RequestBody SupplierRequest request,
            Authentication authentication) {

        SupplierResponse response =
            supplierService.updateSupplier(
                id,
                request,
                authentication.getName()
            );

        return ResponseEntity.ok(
            new ApiResponse<>(
                true,
                "Supplier updated successfully",
                response
            )
        );
    }


    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<SupplierResponse>>
    updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody SupplierStatusRequest request,
            Authentication authentication) {

        SupplierResponse response =
            supplierService.updateStatus(
                id,
                request.getActive(),
                authentication.getName()
            );

        return ResponseEntity.ok(
            new ApiResponse<>(
                true,
                "Supplier status updated successfully",
                response
            )
        );
    }
}