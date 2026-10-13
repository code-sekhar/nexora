package com.nexora.customer.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.nexora.customer.dto.CustomerRequest;
import com.nexora.customer.dto.CustomerResponse;
import com.nexora.customer.dto.CustomerStatusRequest;
import com.nexora.customer.service.CustomerService;
import com.nexora.response.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(
            CustomerService customerService) {

        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>>
    createCustomer(
            @Valid @RequestBody CustomerRequest request,
            Authentication authentication) {

        CustomerResponse response =
                customerService.createCustomer(
                        request,
                        authentication.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        new ApiResponse<>(
                                true,
                                "Customer created successfully",
                                response
                        )
                );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponse>>>
    getAllCustomers(
            Authentication authentication) {

        List<CustomerResponse> response =
                customerService.getAllCustomers(
                        authentication.getName()
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Customers fetched successfully",
                        response
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>>
    getCustomerById(
            @PathVariable Long id,
            Authentication authentication) {

        CustomerResponse response =
                customerService.getCustomerById(
                        id,
                        authentication.getName()
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Customer fetched successfully",
                        response
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>>
    updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request,
            Authentication authentication) {

        CustomerResponse response =
                customerService.updateCustomer(
                        id,
                        request,
                        authentication.getName()
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Customer updated successfully",
                        response
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<CustomerResponse>>
    updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody CustomerStatusRequest request,
            Authentication authentication) {

        CustomerResponse response =
                customerService.updateStatus(
                        id,
                        request.getActive(),
                        authentication.getName()
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Customer status updated successfully",
                        response
                )
        );
    }
}