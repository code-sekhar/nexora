package com.nexora.supplier.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.business.entity.Business;
import com.nexora.purchase.entity.Purchase;
import com.nexora.purchase.repository.PurchaseRepository;
import com.nexora.supplier.dto.SupplierPaymentRequest;
import com.nexora.supplier.dto.SupplierPaymentResponse;
import com.nexora.supplier.entity.Supplier;
import com.nexora.supplier.entity.SupplierPayment;
import com.nexora.supplier.repository.SupplierPaymentRepository;
import com.nexora.supplier.repository.SupplierRepository;
import com.nexora.user.entity.User;

@Service
@Transactional
public class SupplierPaymentService {

    private final SupplierPaymentRepository supplierPaymentRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseRepository purchaseRepository;
    private final SupplierLedgerService supplierLedgerService;

    public SupplierPaymentService(
            SupplierPaymentRepository supplierPaymentRepository,
            SupplierRepository supplierRepository,
            PurchaseRepository purchaseRepository,
            SupplierLedgerService supplierLedgerService) {

        this.supplierPaymentRepository =
                supplierPaymentRepository;

        this.supplierRepository =
                supplierRepository;

        this.purchaseRepository =
                purchaseRepository;

        this.supplierLedgerService =
                supplierLedgerService;
    }

    /*
     * CREATE SUPPLIER PAYMENT
     */
    public SupplierPaymentResponse createPayment(
            Business business,
            User user,
            SupplierPaymentRequest request) {

        if (request.getSupplierId() == null) {
            throw new IllegalArgumentException(
                    "Supplier ID is required"
            );
        }

        if (request.getAmount() == null ||
                request.getAmount()
                        .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero"
            );
        }

        if (request.getPaymentMethod() == null) {
            throw new IllegalArgumentException(
                    "Payment method is required"
            );
        }

        /*
         * Supplier must belong to current business.
         */
        Supplier supplier =
                supplierRepository
                        .findByIdAndBusinessId(
                                request.getSupplierId(),
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Supplier not found"
                                )
                        );

        if (!supplier.isActive()) {
            throw new IllegalArgumentException(
                    "Supplier is inactive"
            );
        }

        Purchase purchase = null;

        /*
         * Optional purchase allocation.
         */
        if (request.getPurchaseId() != null) {

            purchase =
                    purchaseRepository
                            .findById(
                                    request.getPurchaseId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Purchase not found"
                                    )
                            );

            /*
             * Business protection.
             */
            if (!purchase.getBusiness()
                    .getId()
                    .equals(business.getId())) {

                throw new IllegalArgumentException(
                        "Purchase does not belong to this business"
                );
            }

            /*
             * Supplier protection.
             */
            if (!purchase.getSupplier()
                    .getId()
                    .equals(supplier.getId())) {

                throw new IllegalArgumentException(
                        "Purchase does not belong to this supplier"
                );
            }

            /*
             * Cancelled purchase cannot receive payment.
             */
            if (purchase.getStatus()
                    == Purchase.PurchaseStatus.CANCELLED) {

                throw new IllegalArgumentException(
                        "Cannot make payment for cancelled purchase"
                );
            }

            /*
             * Calculate actual payments already recorded.
             */
            BigDecimal alreadyPaid =
                    supplierPaymentRepository
                            .findTotalAmountByPurchaseIdAndBusinessId(
                                    purchase.getId(),
                                    business.getId()
                            );

            if (alreadyPaid == null) {
                alreadyPaid = BigDecimal.ZERO;
            }

            BigDecimal outstanding =
                    purchase.getGrandTotal()
                            .subtract(alreadyPaid);

            if (request.getAmount()
                    .compareTo(outstanding) > 0) {

                throw new IllegalArgumentException(
                        "Payment amount exceeds purchase outstanding amount"
                );
            }
        }

        /*
         * Create payment.
         */
        SupplierPayment payment =
                new SupplierPayment();

        payment.setBusiness(business);
        payment.setSupplier(supplier);
        payment.setPurchase(purchase);

        payment.setAmount(
                request.getAmount()
        );

        payment.setPaymentMethod(
                request.getPaymentMethod()
        );

        payment.setReferenceNumber(
                request.getReferenceNumber()
        );

        payment.setNotes(
                request.getNotes()
        );

        payment.setPaidBy(user);

        if (request.getPaymentDate() != null) {
            payment.setPaymentDate(
                    request.getPaymentDate()
            );
        }

        SupplierPayment saved =
                supplierPaymentRepository.save(payment);

        /*
         * Create supplier ledger CREDIT.
         */
        supplierLedgerService.createPaymentEntry(
                business,
                supplier,
                saved
        );

        /*
         * Synchronize purchase payment values.
         */
        if (purchase != null) {

            syncPurchasePayment(
                    business,
                    purchase
            );
        }

        return mapToResponse(saved);
    }

    /*
     * SYNC PURCHASE PAID/DUE AMOUNT
     */
    private void syncPurchasePayment(
            Business business,
            Purchase purchase) {

        BigDecimal totalPaid =
                supplierPaymentRepository
                        .findTotalAmountByPurchaseIdAndBusinessId(
                                purchase.getId(),
                                business.getId()
                        );

        if (totalPaid == null) {
            totalPaid = BigDecimal.ZERO;
        }

        BigDecimal grandTotal =
                purchase.getGrandTotal() != null
                        ? purchase.getGrandTotal()
                        : BigDecimal.ZERO;

        BigDecimal due =
                grandTotal.subtract(totalPaid);

        if (due.compareTo(BigDecimal.ZERO) < 0) {
            due = BigDecimal.ZERO;
        }

        purchase.setPaidAmount(totalPaid);
        purchase.setDueAmount(due);

        purchaseRepository.save(purchase);
    }

    /*
     * GET ALL PAYMENTS
     */
    @Transactional(readOnly = true)
    public List<SupplierPaymentResponse> getAllPayments(
            Business business) {

        return supplierPaymentRepository
                .findByBusinessIdOrderByPaymentDateDesc(
                        business.getId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /*
     * GET PAYMENT BY ID
     */
    @Transactional(readOnly = true)
    public SupplierPaymentResponse getPaymentById(
            Business business,
            Long paymentId) {

        SupplierPayment payment =
                supplierPaymentRepository
                        .findById(paymentId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Supplier payment not found"
                                )
                        );

        if (!payment.getBusiness()
                .getId()
                .equals(business.getId())) {

            throw new IllegalArgumentException(
                    "Supplier payment does not belong to this business"
            );
        }

        return mapToResponse(payment);
    }

    /*
     * GET SUPPLIER PAYMENTS
     */
    @Transactional(readOnly = true)
    public List<SupplierPaymentResponse> getSupplierPayments(
            Business business,
            Supplier supplier) {

        return supplierPaymentRepository
                .findBySupplierIdAndBusinessIdOrderByPaymentDateAsc(
                        supplier.getId(),
                        business.getId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /*
     * GET PURCHASE PAYMENTS
     */
    @Transactional(readOnly = true)
    public List<SupplierPaymentResponse> getPurchasePayments(
            Business business,
            Long purchaseId) {

        return supplierPaymentRepository
                .findByPurchaseIdAndBusinessIdOrderByPaymentDateAsc(
                        purchaseId,
                        business.getId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /*
     * MAP ENTITY → RESPONSE
     */
    private SupplierPaymentResponse mapToResponse(
            SupplierPayment payment) {

        SupplierPaymentResponse response =
                new SupplierPaymentResponse();

        response.setId(
                payment.getId()
        );

        response.setSupplierId(
                payment.getSupplier().getId()
        );

        response.setSupplierName(
                payment.getSupplier().getName()
        );

        if (payment.getPurchase() != null) {

            response.setPurchaseId(
                    payment.getPurchase().getId()
            );

            response.setPurchaseReference(
                    "PURCHASE-" +
                    payment.getPurchase().getId()
            );
        }

        response.setAmount(
                payment.getAmount()
        );

        response.setPaymentMethod(
                payment.getPaymentMethod().name()
        );

        response.setReferenceNumber(
                payment.getReferenceNumber()
        );

        response.setNotes(
                payment.getNotes()
        );

        if (payment.getPaidBy() != null) {

            response.setPaidById(
                    payment.getPaidBy().getId()
            );

            response.setPaidByName(
                    payment.getPaidBy().getName()
            );
        }

        response.setPaymentDate(
                payment.getPaymentDate()
        );

        response.setCreatedAt(
                payment.getCreatedAt()
        );

        return response;
    }
}