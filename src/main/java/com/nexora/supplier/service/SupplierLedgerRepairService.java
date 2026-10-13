package com.nexora.supplier.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.business.entity.Business;
import com.nexora.purchase.entity.Purchase;
import com.nexora.purchase.repository.PurchaseRepository;
import com.nexora.supplier.entity.Supplier;
import com.nexora.supplier.entity.SupplierLedger;
import com.nexora.supplier.entity.SupplierPayment;
import com.nexora.supplier.repository.SupplierLedgerRepository;
import com.nexora.supplier.repository.SupplierPaymentRepository;

@Service
@Transactional
public class SupplierLedgerRepairService {

    private final SupplierLedgerRepository supplierLedgerRepository;
    private final SupplierPaymentRepository supplierPaymentRepository;
    private final PurchaseRepository purchaseRepository;

    public SupplierLedgerRepairService(
            SupplierLedgerRepository supplierLedgerRepository,
            SupplierPaymentRepository supplierPaymentRepository,
            PurchaseRepository purchaseRepository) {

        this.supplierLedgerRepository =
                supplierLedgerRepository;

        this.supplierPaymentRepository =
                supplierPaymentRepository;

        this.purchaseRepository =
                purchaseRepository;
    }

    /*
     * REPAIR ALL SUPPLIER LEDGER LINKS
     */
    public int repairBusiness(Business business) {

        List<SupplierLedger> entries =
                supplierLedgerRepository
                        .findByBusinessIdOrderByCreatedAtAsc(
                                business.getId()
                        );

        int repaired = 0;

        for (SupplierLedger ledger : entries) {

            if (ledger.getType()
                    == SupplierLedger.LedgerType.PURCHASE
                    && ledger.getPurchase() == null) {

                Purchase purchase =
                        findMatchingPurchase(
                                business,
                                ledger
                        );

                if (purchase != null) {

                    ledger.setPurchase(purchase);

                    repaired++;
                }
            }

            if (ledger.getType()
                    == SupplierLedger.LedgerType.PAYMENT
                    && ledger.getPayment() == null) {

                SupplierPayment payment =
                        findMatchingPayment(
                                business,
                                ledger
                        );

                if (payment != null) {

                    ledger.setPayment(payment);

                    repaired++;
                }
            }
        }

        supplierLedgerRepository.saveAll(entries);

        /*
         * Rebuild all supplier balances.
         */
        rebuildAllBalances(
                business,
                entries
        );

        return repaired;
    }

    /*
     * FIND PURCHASE FOR OLD LEDGER ENTRY
     */
    private Purchase findMatchingPurchase(
            Business business,
            SupplierLedger ledger) {

        String description =
                ledger.getDescription();

        if (description == null ||
                description.isBlank()) {

            return null;
        }

        /*
         * Example:
         *
         * Purchase PUR-1790076862777-F123
         *
         * We extract:
         *
         * PUR-1790076862777-F123
         */
        String prefix = "Purchase ";

        if (!description.startsWith(prefix)) {
            return null;
        }

        String purchaseNumber =
                description.substring(
                        prefix.length()
                ).trim();

        if (purchaseNumber.isBlank()) {
            return null;
        }

        /*
         * Exact business + purchase number match.
         */
        Purchase purchase =
                purchaseRepository
                        .findByPurchaseNumberAndBusinessId(
                                purchaseNumber,
                                business.getId()
                        )
                        .orElse(null);

        if (purchase == null) {
            return null;
        }

        /*
         * Supplier safety check.
         */
        if (purchase.getSupplier() == null ||
                ledger.getSupplier() == null) {

            return null;
        }

        if (!purchase.getSupplier()
                .getId()
                .equals(
                        ledger.getSupplier()
                                .getId()
                )) {

            return null;
        }

        /*
         * Amount safety check.
         */
        BigDecimal purchaseAmount =
                purchase.getGrandTotal() != null
                        ? purchase.getGrandTotal()
                        : BigDecimal.ZERO;

        BigDecimal ledgerDebit =
                ledger.getDebit() != null
                        ? ledger.getDebit()
                        : BigDecimal.ZERO;

        if (purchaseAmount.compareTo(
                ledgerDebit
        ) != 0) {

            return null;
        }

        return purchase;
    }

    /*
     * FIND PAYMENT FOR OLD LEDGER ENTRY
     */
    private SupplierPayment findMatchingPayment(
            Business business,
            SupplierLedger ledger) {

        List<SupplierPayment> payments =
                supplierPaymentRepository
                        .findBySupplierIdAndBusinessIdOrderByPaymentDateAsc(
                                ledger.getSupplier().getId(),
                                business.getId()
                        );

        for (SupplierPayment payment : payments) {

            if (payment.getAmount() == null) {
                continue;
            }

            BigDecimal ledgerCredit =
                    ledger.getCredit() != null
                            ? ledger.getCredit()
                            : BigDecimal.ZERO;

            /*
             * Amount match.
             */
            if (payment.getAmount()
                    .compareTo(ledgerCredit) != 0) {

                continue;
            }

            /*
             * Payment date match.
             */
            if (payment.getPaymentDate() != null
                    && ledger.getCreatedAt() != null) {

                if (!payment.getPaymentDate()
                        .equals(
                                ledger.getCreatedAt()
                        )) {

                    continue;
                }
            }

            /*
             * If ledger description contains
             * payment ID, use it as additional check.
             */
            String description =
                    ledger.getDescription();

            if (description != null
                    && description.contains("#")) {

                String paymentIdText =
                        description.substring(
                                description.lastIndexOf("#") + 1
                        ).trim();

                try {

                    Long paymentId =
                            Long.parseLong(
                                    paymentIdText
                            );

                    if (payment.getId()
                            .equals(paymentId)) {

                        return payment;
                    }

                } catch (NumberFormatException ignored) {
                    /*
                     * Continue with amount/date matching.
                     */
                }
            }

            return payment;
        }

        return null;
    }

    /*
     * REBUILD ALL SUPPLIER BALANCES
     */
    private void rebuildAllBalances(
            Business business,
            List<SupplierLedger> entries) {

        /*
         * Group by supplier.
         */
        java.util.Map<Long, BigDecimal> balances =
                new java.util.HashMap<>();

        for (SupplierLedger ledger : entries) {

            Long supplierId =
                    ledger.getSupplier()
                            .getId();

            BigDecimal current =
                    balances.getOrDefault(
                            supplierId,
                            BigDecimal.ZERO
                    );

            BigDecimal debit =
                    ledger.getDebit() != null
                            ? ledger.getDebit()
                            : BigDecimal.ZERO;

            BigDecimal credit =
                    ledger.getCredit() != null
                            ? ledger.getCredit()
                            : BigDecimal.ZERO;

            BigDecimal balance =
                    current
                            .add(debit)
                            .subtract(credit);

            ledger.setBalance(balance);

            balances.put(
                    supplierId,
                    balance
            );
        }

        supplierLedgerRepository.saveAll(entries);
    }
}