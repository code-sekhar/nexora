package com.nexora.supplier.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.nexora.business.entity.Business;
import com.nexora.business.repository.BusinessRepository;
import com.nexora.supplier.dto.SupplierPaymentRequest;
import com.nexora.supplier.dto.SupplierPaymentResponse;
import com.nexora.supplier.entity.Supplier;
import com.nexora.supplier.repository.SupplierRepository;
import com.nexora.supplier.service.SupplierPaymentService;
import com.nexora.user.entity.User;
import com.nexora.user.repository.UserRepository;

@RestController
@RequestMapping("/api/supplier-payments")
public class SupplierPaymentController {

    private final SupplierPaymentService supplierPaymentService;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;

    public SupplierPaymentController(
            SupplierPaymentService supplierPaymentService,
            SupplierRepository supplierRepository,
            UserRepository userRepository,
            BusinessRepository businessRepository) {

        this.supplierPaymentService =
                supplierPaymentService;

        this.supplierRepository =
                supplierRepository;

        this.userRepository =
                userRepository;

        this.businessRepository =
                businessRepository;
    }

    /*
     * CREATE PAYMENT
     */
    @PostMapping
    public ResponseEntity<?> createPayment(
            Authentication authentication,
            @RequestBody SupplierPaymentRequest request) {

        User user =
                getUser(authentication);

        Business business =
                getBusiness(user);

        SupplierPaymentResponse response =
                supplierPaymentService.createPayment(
                        business,
                        user,
                        request
                );

        return ResponseEntity.ok(
                java.util.Map.of(
                        "success", true,
                        "message",
                        "Supplier payment created successfully",
                        "data", response
                )
        );
    }

    /*
     * GET ALL PAYMENTS
     */
    @GetMapping
    public ResponseEntity<?> getAllPayments(
            Authentication authentication) {

        User user =
                getUser(authentication);

        Business business =
                getBusiness(user);

        List<SupplierPaymentResponse> data =
                supplierPaymentService
                        .getAllPayments(business);

        return ResponseEntity.ok(
                java.util.Map.of(
                        "success", true,
                        "message",
                        "Supplier payments fetched successfully",
                        "data", data
                )
        );
    }

    /*
     * GET PAYMENT BY ID
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getPaymentById(
            Authentication authentication,
            @PathVariable Long paymentId) {

        User user =
                getUser(authentication);

        Business business =
                getBusiness(user);

        SupplierPaymentResponse data =
                supplierPaymentService
                        .getPaymentById(
                                business,
                                paymentId
                        );

        return ResponseEntity.ok(
                java.util.Map.of(
                        "success", true,
                        "message",
                        "Supplier payment fetched successfully",
                        "data", data
                )
        );
    }

    /*
     * GET SUPPLIER PAYMENTS
     */
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<?> getSupplierPayments(
            Authentication authentication,
            @PathVariable Long supplierId) {

        User user =
                getUser(authentication);

        Business business =
                getBusiness(user);

        Supplier supplier =
                supplierRepository
                        .findByIdAndBusinessId(
                                supplierId,
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Supplier not found"
                                )
                        );

        List<SupplierPaymentResponse> data =
                supplierPaymentService
                        .getSupplierPayments(
                                business,
                                supplier
                        );

        return ResponseEntity.ok(
                java.util.Map.of(
                        "success", true,
                        "message",
                        "Supplier payments fetched successfully",
                        "data", data
                )
        );
    }

    /*
     * GET PURCHASE PAYMENTS
     */
    @GetMapping("/purchase/{purchaseId}")
    public ResponseEntity<?> getPurchasePayments(
            Authentication authentication,
            @PathVariable Long purchaseId) {

        User user =
                getUser(authentication);

        Business business =
                getBusiness(user);

        List<SupplierPaymentResponse> data =
                supplierPaymentService
                        .getPurchasePayments(
                                business,
                                purchaseId
                        );

        return ResponseEntity.ok(
                java.util.Map.of(
                        "success", true,
                        "message",
                        "Purchase payments fetched successfully",
                        "data", data
                )
        );
    }

    private User getUser(
            Authentication authentication) {

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );
    }

    private Business getBusiness(User user) {

        return businessRepository
                .findById(
                        user.getBusiness().getId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Business not found"
                        )
                );
    }
}