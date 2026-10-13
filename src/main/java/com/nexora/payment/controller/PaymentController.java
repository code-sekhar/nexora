package com.nexora.payment.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.nexora.payment.dto.PaymentRequest;
import com.nexora.payment.dto.PaymentResponse;
import com.nexora.payment.service.PaymentService;
import com.nexora.response.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(
            PaymentService paymentService) {

        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>>
            createPayment(
                    @Valid @RequestBody PaymentRequest request,
                    java.security.Principal principal) {

        PaymentResponse response =
                paymentService.createPayment(
                        request,
                        principal.getName()
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Payment created successfully",
                        response
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponse>>>
            getAllPayments(
                    java.security.Principal principal) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Payments fetched successfully",
                        paymentService.getAllPayments(
                                principal.getName()
                        )
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>>
            getPaymentById(
                    @PathVariable Long id,
                    java.security.Principal principal) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Payment fetched successfully",
                        paymentService.getPaymentById(
                                id,
                                principal.getName()
                        )
                )
        );
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>>
            getCustomerPayments(
                    @PathVariable Long customerId,
                    java.security.Principal principal) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Customer payments fetched successfully",
                        paymentService.getCustomerPayments(
                                customerId,
                                principal.getName()
                        )
                )
        );
    }

    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>>
            getInvoicePayments(
                    @PathVariable Long invoiceId,
                    java.security.Principal principal) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Invoice payments fetched successfully",
                        paymentService.getInvoicePayments(
                                invoiceId,
                                principal.getName()
                        )
                )
        );
    }
}