package com.nexora.customer.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.nexora.business.entity.Business;
import com.nexora.customer.dto.CustomerLedgerResponse;
import com.nexora.customer.dto.CustomerLedgerSummaryResponse;
import com.nexora.customer.entity.Customer;
import com.nexora.customer.repository.CustomerRepository;
import com.nexora.customer.service.CustomerLedgerReconciliationService;
import com.nexora.customer.service.CustomerLedgerService;
import com.nexora.exception.ResourceNotFoundException;
import com.nexora.response.ApiResponse;
import com.nexora.user.entity.User;
import com.nexora.user.repository.UserRepository;

@RestController
@RequestMapping("/api/customer-ledger")
public class CustomerLedgerController {

    private final CustomerLedgerService customerLedgerService;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final CustomerLedgerReconciliationService reconciliationService;

    public CustomerLedgerController(
            CustomerLedgerService customerLedgerService,
            CustomerRepository customerRepository,
            UserRepository userRepository,
            CustomerLedgerReconciliationService reconciliationService) {

        this.customerLedgerService = customerLedgerService;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.reconciliationService = reconciliationService;
    }

    /*
     * GET ALL CUSTOMER LEDGER
     */
    @GetMapping
    public ResponseEntity<?> getAllLedger(
            Authentication authentication) {

        User user = getUser(authentication.getName());

        Business business = getBusiness(user);

        List<CustomerLedgerResponse> data =
                customerLedgerService.getAllLedger(business);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Customer ledger fetched successfully",
                        data
                )
        );
    }

    /*
     * GET CUSTOMER LEDGER
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getCustomerLedger(
            @PathVariable Long customerId,
            Authentication authentication) {

        User user = getUser(authentication.getName());

        Business business = getBusiness(user);

        Customer customer =
                customerRepository
                        .findByIdAndBusinessId(
                                customerId,
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found"
                                )
                        );

        CustomerLedgerSummaryResponse data =
                customerLedgerService.getCustomerLedger(
                        business,
                        customer
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Customer ledger fetched successfully",
                        data
                )
        );
    }

    /*
     * GET CUSTOMER CURRENT BALANCE
     */
    @GetMapping("/customer/{customerId}/balance")
    public ResponseEntity<?> getCustomerBalance(
            @PathVariable Long customerId,
            Authentication authentication) {

        User user = getUser(authentication.getName());

        Business business = getBusiness(user);

        Customer customer =
                customerRepository
                        .findByIdAndBusinessId(
                                customerId,
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found"
                                )
                        );

        BigDecimal balance =
                customerLedgerService.getCurrentBalance(
                        customer.getId(),
                        business.getId()
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Customer balance fetched successfully",
                        balance
                )
        );
    }

    /*
     * RECONCILE ALL CUSTOMER LEDGERS
     */
    @PostMapping("/reconcile")
    public ResponseEntity<?> reconcile(
            Authentication authentication) {

        User user = getUser(authentication.getName());

        Business business = getBusiness(user);

        int createdEntries =
                reconciliationService.reconcileBusiness(business);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Customer ledger reconciliation completed",
                        createdEntries
                )
        );
    }

    /*
     * REPAIR HISTORICAL LEDGER DATES
     *
     * Example:
     * POST /api/customer-ledger/repair/1
     */
    @PostMapping("/repair/{customerId}")
    public ResponseEntity<?> repairLedger(
            @PathVariable Long customerId,
            Authentication authentication) {

        User user = getUser(authentication.getName());

        Business business = getBusiness(user);

        Customer customer =
                customerRepository
                        .findByIdAndBusinessId(
                                customerId,
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found"
                                )
                        );

        int repaired =
                reconciliationService.repairHistoricalLedgerDates(
                        business,
                        customer
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Customer ledger historical dates repaired",
                        repaired
                )
        );
    }

    /*
     * GET CURRENT USER
     */
    private User getUser(String email) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }

    /*
     * GET CURRENT BUSINESS
     */
    private Business getBusiness(User user) {

        if (user.getBusiness() == null) {

            throw new ResourceNotFoundException(
                    "Business not assigned to user"
            );
        }

        return user.getBusiness();
    }
}