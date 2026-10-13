package com.nexora.supplier.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.nexora.business.entity.Business;
import com.nexora.customer.repository.CustomerRepository;
import com.nexora.exception.ResourceNotFoundException;
import com.nexora.response.ApiResponse;
import com.nexora.supplier.dto.SupplierLedgerResponse;
import com.nexora.supplier.dto.SupplierLedgerSummaryResponse;
import com.nexora.supplier.entity.Supplier;
import com.nexora.supplier.repository.SupplierRepository;
import com.nexora.supplier.service.SupplierLedgerReconciliationService;
import com.nexora.supplier.service.SupplierLedgerService;
import com.nexora.user.entity.User;
import com.nexora.user.repository.UserRepository;
import com.nexora.supplier.service.SupplierLedgerRepairService;

@RestController
@RequestMapping("/api/supplier-ledger")
public class SupplierLedgerController {

    private final SupplierLedgerService supplierLedgerService;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final SupplierLedgerReconciliationService reconciliationService;
    private final SupplierLedgerRepairService repairService;

    public SupplierLedgerController(
            SupplierLedgerService supplierLedgerService,
            SupplierRepository supplierRepository,
            UserRepository userRepository,
            SupplierLedgerReconciliationService reconciliationService,
            SupplierLedgerRepairService repairService) {

        this.supplierLedgerService =
                supplierLedgerService;

        this.supplierRepository =
                supplierRepository;

        this.userRepository =
                userRepository;
        this.reconciliationService =
                reconciliationService;
        this.repairService = repairService;
    }

    /*
     * GET ALL SUPPLIER LEDGER
     */
    @GetMapping
    public ResponseEntity<?> getAllLedger(
            Authentication authentication) {

        User user =
                getUser(authentication.getName());

        Business business =
                getBusiness(user);

        List<SupplierLedgerResponse> data =
                supplierLedgerService
                        .getAllLedger(business);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Supplier ledger fetched successfully",
                        data
                )
        );
    }

    /*
     * GET SUPPLIER LEDGER
     */
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<?> getSupplierLedger(
            @PathVariable Long supplierId,
            Authentication authentication) {

        User user =
                getUser(authentication.getName());

        Business business =
                getBusiness(user);

        Supplier supplier =
                supplierRepository
                        .findByIdAndBusinessId(
                                supplierId,
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Supplier not found"
                                )
                        );

        SupplierLedgerSummaryResponse data =
                supplierLedgerService
                        .getSupplierLedger(
                                business,
                                supplier
                        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Supplier ledger fetched successfully",
                        data
                )
        );
    }

    /*
     * GET SUPPLIER CURRENT DUE
     */
    @GetMapping("/supplier/{supplierId}/due")
    public ResponseEntity<?> getSupplierDue(
            @PathVariable Long supplierId,
            Authentication authentication) {

        User user =
                getUser(authentication.getName());

        Business business =
                getBusiness(user);

        Supplier supplier =
                supplierRepository
                        .findByIdAndBusinessId(
                                supplierId,
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Supplier not found"
                                )
                        );

        BigDecimal due =
                supplierLedgerService
                        .getCurrentDue(
                                supplier.getId(),
                                business.getId()
                        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Supplier current due fetched successfully",
                        due
                )
        );
    }

    private User getUser(String email) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }

    private Business getBusiness(User user) {

        if (user.getBusiness() == null) {

            throw new ResourceNotFoundException(
                    "Business not assigned to user"
            );
        }

        return user.getBusiness();
    }
    @PostMapping("/reconcile")
    public ResponseEntity<?> reconcile(
            Authentication authentication) {

        User user =
                getUser(authentication.getName());

        Business business =
                getBusiness(user);

        int createdEntries =
                reconciliationService
                        .reconcileBusiness(business);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Supplier ledger reconciliation completed",
                        createdEntries
                )
        );
    }
    @PostMapping("/repair")
    public ResponseEntity<?> repairLedger(
            Authentication authentication) {

        User user =
                getUser(authentication.getName());

        Business business =
                getBusiness(user);

        int repaired =
                repairService.repairBusiness(
                        business
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Supplier ledger repair completed",
                        repaired
                )
        );
    }
}