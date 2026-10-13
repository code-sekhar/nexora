package com.nexora.supplier.statement.controller;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.nexora.business.entity.Business;
import com.nexora.exception.ResourceNotFoundException;
import com.nexora.response.ApiResponse;
import com.nexora.supplier.entity.Supplier;
import com.nexora.supplier.repository.SupplierRepository;
import com.nexora.supplier.statement.dto.SupplierStatementResponse;
import com.nexora.supplier.statement.service.SupplierStatementService;
import com.nexora.user.entity.User;
import com.nexora.user.repository.UserRepository;

@RestController
@RequestMapping("/api/supplier-statement")
public class SupplierStatementController {

    private final SupplierStatementService supplierStatementService;

    private final SupplierRepository supplierRepository;

    private final UserRepository userRepository;

    public SupplierStatementController(
            SupplierStatementService supplierStatementService,
            SupplierRepository supplierRepository,
            UserRepository userRepository) {

        this.supplierStatementService =
                supplierStatementService;

        this.supplierRepository =
                supplierRepository;

        this.userRepository =
                userRepository;
    }

    /*
     * GET SUPPLIER STATEMENT
     *
     * Example:
     *
     * GET /api/supplier-statement/supplier/1
     *
     * GET /api/supplier-statement/supplier/1
     *     ?from=2026-09-01
     *     &to=2026-09-30
     */
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<?> getSupplierStatement(

            @PathVariable Long supplierId,

            @RequestParam(required = false)
            LocalDate from,

            @RequestParam(required = false)
            LocalDate to,

            Authentication authentication) {

        User user =
                getUser(
                        authentication.getName()
                );

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

        SupplierStatementResponse data =
                supplierStatementService
                        .getStatement(
                                business,
                                supplier,
                                from,
                                to
                        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Supplier statement fetched successfully",
                        data
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
}