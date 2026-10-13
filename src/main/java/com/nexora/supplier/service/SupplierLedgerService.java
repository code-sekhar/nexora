package com.nexora.supplier.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.business.entity.Business;
import com.nexora.purchase.entity.Purchase;
import com.nexora.purchase.returnmodule.entity.PurchaseReturn;
import com.nexora.supplier.dto.SupplierLedgerResponse;
import com.nexora.supplier.dto.SupplierLedgerSummaryResponse;
import com.nexora.supplier.entity.Supplier;
import com.nexora.supplier.entity.SupplierLedger;
import com.nexora.supplier.entity.SupplierPayment;
import com.nexora.supplier.repository.SupplierLedgerRepository;
import java.time.LocalDateTime;



@Service
@Transactional
public class SupplierLedgerService {

    private final SupplierLedgerRepository supplierLedgerRepository;

    public SupplierLedgerService(
            SupplierLedgerRepository supplierLedgerRepository) {

        this.supplierLedgerRepository =
                supplierLedgerRepository;
    }

    /*
     * CREATE OPENING BALANCE
     */
    public SupplierLedger createOpeningBalance(
            Business business,
            Supplier supplier,
            BigDecimal amount) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Opening balance must be greater than zero"
            );
        }

        SupplierLedger ledger =
                new SupplierLedger();

        ledger.setBusiness(business);
        ledger.setSupplier(supplier);

        ledger.setType(
                SupplierLedger.LedgerType.OPENING_BALANCE
        );

        ledger.setDebit(amount);
        ledger.setCredit(BigDecimal.ZERO);
        ledger.setBalance(amount);

        ledger.setDescription(
                "Supplier opening balance"
        );

        return supplierLedgerRepository.save(ledger);
    }

    /*
     * CREATE PURCHASE ENTRY
     */
    public SupplierLedger createPurchaseEntry(
            Business business,
            Supplier supplier,
            Purchase purchase) {

        boolean exists =
                supplierLedgerRepository.existsByPurchaseIdAndType(
                        purchase.getId(),
                        SupplierLedger.LedgerType.PURCHASE
                );

        if (exists) {
            return supplierLedgerRepository
                    .findBySupplierIdAndBusinessIdOrderByCreatedAtAsc(
                            supplier.getId(),
                            business.getId()
                    )
                    .stream()
                    .filter(entry ->
                            entry.getPurchase() != null
                                    && entry.getPurchase().getId()
                                    .equals(purchase.getId())
                                    && entry.getType()
                                    == SupplierLedger.LedgerType.PURCHASE
                    )
                    .findFirst()
                    .orElse(null);
        }

        SupplierLedger ledger =
                new SupplierLedger();

        ledger.setBusiness(business);
        ledger.setSupplier(supplier);

        ledger.setType(
                SupplierLedger.LedgerType.PURCHASE
        );

        ledger.setPurchase(purchase);

        ledger.setDebit(
                purchase.getGrandTotal() != null
                        ? purchase.getGrandTotal()
                        : BigDecimal.ZERO
        );

        ledger.setCredit(
                BigDecimal.ZERO
        );

        ledger.setBalance(
                BigDecimal.ZERO
        );

        if (purchase.getCreatedAt() != null) {
            ledger.setCreatedAt(
                    purchase.getCreatedAt()
            );
        }

        ledger.setDescription(
                "Purchase " +
                purchase.getPurchaseNumber()
        );

        SupplierLedger saved =
                supplierLedgerRepository.save(ledger);

        rebuildSupplierBalance(
                business,
                supplier
        );

        return saved;
    }
    
    
    public SupplierLedger createPurchaseReturnEntry(
            Business business,
            Supplier supplier,
            PurchaseReturn purchaseReturn) {

        if (supplierLedgerRepository
                .existsByPurchaseReturnId(
                        purchaseReturn.getId()
                )) {

            return supplierLedgerRepository
                    .findByBusinessIdOrderByCreatedAtAsc(
                            business.getId()
                    )
                    .stream()
                    .filter(ledger ->
                            ledger.getPurchaseReturn() != null
                            && ledger.getPurchaseReturn()
                                    .getId()
                                    .equals(
                                            purchaseReturn.getId()
                                    )
                    )
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "Purchase return ledger entry exists but could not be found"
                            )
                    );
        }

        SupplierLedger ledger =
                new SupplierLedger();

        ledger.setBusiness(business);
        ledger.setSupplier(supplier);

        ledger.setType(
                SupplierLedger.LedgerType.PURCHASE_RETURN
        );

        ledger.setPurchase(
                purchaseReturn.getPurchase()
        );

        ledger.setPurchaseReturn(
                purchaseReturn
        );

        ledger.setDebit(
                BigDecimal.ZERO
        );

        ledger.setCredit(
                purchaseReturn.getTotalAmount()
        );

        ledger.setBalance(
                BigDecimal.ZERO
        );

        ledger.setDescription(
                "Purchase return "
                        + purchaseReturn.getReturnNumber()
        );

        ledger.setCreatedAt(
                LocalDateTime.now()
        );

        SupplierLedger saved =
                supplierLedgerRepository.save(
                        ledger
                );

        rebuildSupplierBalance(
                business,
                supplier
        );

        return saved;
    }

    /*
     * GET ALL LEDGER
     */
    @Transactional(readOnly = true)
    public List<SupplierLedgerResponse> getAllLedger(
            Business business) {

        return supplierLedgerRepository
                .findByBusinessIdOrderByCreatedAtAsc(
                        business.getId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /*
     * GET SUPPLIER LEDGER
     */
    @Transactional(readOnly = true)
    public SupplierLedgerSummaryResponse
    getSupplierLedger(
            Business business,
            Supplier supplier) {

        List<SupplierLedger> entries =
                supplierLedgerRepository
                        .findBySupplierIdAndBusinessIdOrderByCreatedAtAsc(
                                supplier.getId(),
                                business.getId()
                        );

        BigDecimal openingBalance =
                BigDecimal.ZERO;

        BigDecimal totalPurchases =
                BigDecimal.ZERO;

        BigDecimal totalPayments =
                BigDecimal.ZERO;

        BigDecimal totalPurchaseReturns =
                BigDecimal.ZERO;

        BigDecimal currentDue =
                BigDecimal.ZERO;

        List<SupplierLedgerResponse> responseEntries =
                entries.stream()
                        .map(this::mapToResponse)
                        .toList();

        for (SupplierLedger entry : entries) {

            BigDecimal debit =
                    entry.getDebit() != null
                            ? entry.getDebit()
                            : BigDecimal.ZERO;

            BigDecimal credit =
                    entry.getCredit() != null
                            ? entry.getCredit()
                            : BigDecimal.ZERO;

            switch (entry.getType()) {

                case OPENING_BALANCE:
                    openingBalance =
                            openingBalance.add(debit);
                    break;

                case PURCHASE:
                    totalPurchases =
                            totalPurchases.add(debit);
                    break;

                case PAYMENT:
                    totalPayments =
                            totalPayments.add(credit);
                    break;

                case PURCHASE_RETURN:
                    totalPurchaseReturns =
                            totalPurchaseReturns.add(credit);
                    break;

                default:
                    break;
            }

            currentDue =
                    currentDue
                            .add(debit)
                            .subtract(credit);
        }

        SupplierLedgerSummaryResponse response =
                new SupplierLedgerSummaryResponse();

        response.setSupplierId(
                supplier.getId()
        );

        response.setSupplierName(
                supplier.getName()
        );

        response.setOpeningBalance(
                openingBalance
        );

        response.setTotalPurchases(
                totalPurchases
        );

        response.setTotalPayments(
                totalPayments
        );

        response.setTotalPurchaseReturns(
                totalPurchaseReturns
        );

        response.setCurrentDue(
                currentDue
        );

        response.setEntries(
                responseEntries
        );

        return response;
    }

    /*
     * GET CURRENT DUE
     */
    @Transactional(readOnly = true)
    public BigDecimal getCurrentDue(
            Long supplierId,
            Long businessId) {

        List<SupplierLedger> entries =
                supplierLedgerRepository
                        .findBySupplierIdAndBusinessIdOrderByCreatedAtAsc(
                                supplierId,
                                businessId
                        );

        BigDecimal balance =
                BigDecimal.ZERO;

        for (SupplierLedger entry : entries) {

            BigDecimal debit =
                    entry.getDebit() != null
                            ? entry.getDebit()
                            : BigDecimal.ZERO;

            BigDecimal credit =
                    entry.getCredit() != null
                            ? entry.getCredit()
                            : BigDecimal.ZERO;

            balance =
                    balance
                            .add(debit)
                            .subtract(credit);
        }

        return balance;
    }

    /*
     * REBUILD SUPPLIER BALANCE
     */
    public void rebuildSupplierBalance(
            Business business,
            Supplier supplier) {

        List<SupplierLedger> entries =
                supplierLedgerRepository
                        .findBySupplierIdAndBusinessIdOrderByCreatedAtAsc(
                                supplier.getId(),
                                business.getId()
                        );

        BigDecimal balance =
                BigDecimal.ZERO;

        for (SupplierLedger ledger : entries) {

            BigDecimal debit =
                    ledger.getDebit() != null
                            ? ledger.getDebit()
                            : BigDecimal.ZERO;

            BigDecimal credit =
                    ledger.getCredit() != null
                            ? ledger.getCredit()
                            : BigDecimal.ZERO;

            balance =
                    balance
                            .add(debit)
                            .subtract(credit);

            ledger.setBalance(balance);
        }

        supplierLedgerRepository.saveAll(entries);
    }

    /*
     * MAP ENTITY → RESPONSE
     */
    private SupplierLedgerResponse mapToResponse(
            SupplierLedger ledger) {

        SupplierLedgerResponse response =
                new SupplierLedgerResponse();

        response.setId(
                ledger.getId()
        );

        response.setSupplierId(
                ledger.getSupplier().getId()
        );

        response.setSupplierName(
                ledger.getSupplier().getName()
        );

        /*
         * PURCHASE LINK
         */
        if (ledger.getPurchase() != null) {

            response.setPurchaseId(
                    ledger.getPurchase().getId()
            );

            response.setPurchaseReference(
                    "PURCHASE-" +
                    ledger.getPurchase().getId()
            );
        }

        /*
         * PAYMENT LINK
         */
        if (ledger.getPayment() != null) {

            response.setPaymentId(
                    ledger.getPayment().getId()
            );

            response.setPaymentReference(
                    ledger.getPayment().getReferenceNumber()
            );
        }

        response.setType(
                ledger.getType().name()
        );

        response.setDebit(
                ledger.getDebit()
        );

        response.setCredit(
                ledger.getCredit()
        );

        response.setBalance(
                ledger.getBalance()
        );

        response.setDescription(
                ledger.getDescription()
        );

        response.setCreatedAt(
                ledger.getCreatedAt()
        );

        return response;
    }
    /*
     * CREATE SUPPLIER PAYMENT LEDGER ENTRY
     */
    public SupplierLedger createPaymentEntry(
            Business business,
            Supplier supplier,
            SupplierPayment payment) {

        SupplierLedger ledger =
                new SupplierLedger();

        ledger.setBusiness(business);
        ledger.setSupplier(supplier);

        ledger.setType(
                SupplierLedger.LedgerType.PAYMENT
        );

        ledger.setPayment(payment);

        ledger.setDebit(
                BigDecimal.ZERO
        );

        ledger.setCredit(
                payment.getAmount()
        );

        ledger.setBalance(
                BigDecimal.ZERO
        );

        ledger.setDescription(
                "Supplier payment #" +
                payment.getId()
        );

        ledger.setCreatedAt(
                payment.getPaymentDate()
        );

        SupplierLedger saved =
                supplierLedgerRepository.save(ledger);

        rebuildSupplierBalance(
                business,
                supplier
        );

        return saved;
    }
    public int reconcilePaymentLedger(
            Business business,
            SupplierPayment payment) {

        Optional<SupplierLedger> existing =
                supplierLedgerRepository
                        .findByPaymentId(payment.getId());

        if (existing.isPresent()) {
            return 0;
        }

        /*
         * Find old PAYMENT ledger entry
         * created for this payment.
         */
        List<SupplierLedger> entries =
                supplierLedgerRepository
                        .findBySupplierIdAndBusinessIdOrderByCreatedAtAsc(
                                payment.getSupplier().getId(),
                                business.getId()
                        );

        for (SupplierLedger ledger : entries) {

            if (ledger.getType()
                    == SupplierLedger.LedgerType.PAYMENT
                    && ledger.getPayment() == null
                    && ledger.getCredit() != null
                    && ledger.getCredit()
                        .compareTo(payment.getAmount()) == 0) {

                ledger.setPayment(payment);

                supplierLedgerRepository.save(ledger);

                return 1;
            }
        }

        createPaymentEntry(
                business,
                payment.getSupplier(),
                payment
        );

        return 1;
    }
    /*
     * CREATE PURCHASE CANCELLATION LEDGER ENTRY
     *
     * Used when an entire purchase is cancelled.
     *
     * This is different from an actual Purchase Return:
     *
     * Purchase Cancellation:
     *     Credit = complete purchase grand total
     *
     * Purchase Return:
     *     Credit = actual returned amount
     */
    public SupplierLedger createPurchaseCancellationEntry(
            Business business,
            Supplier supplier,
            Purchase purchase) {

        /*
         * Prevent duplicate cancellation ledger entry.
         */
        boolean exists =
                supplierLedgerRepository
                        .existsByPurchaseIdAndType(
                                purchase.getId(),
                                SupplierLedger.LedgerType.PURCHASE_RETURN
                        );

        if (exists) {

            return supplierLedgerRepository
                    .findBySupplierIdAndBusinessIdOrderByCreatedAtAsc(
                            supplier.getId(),
                            business.getId()
                    )
                    .stream()
                    .filter(ledger ->
                            ledger.getPurchase() != null
                                    && ledger.getPurchase()
                                            .getId()
                                            .equals(purchase.getId())
                                    && ledger.getType()
                                            == SupplierLedger.LedgerType.PURCHASE_RETURN
                                    && ledger.getPurchaseReturn() == null
                    )
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "Purchase cancellation ledger entry exists but could not be found"
                            )
                    );
        }

        SupplierLedger ledger =
                new SupplierLedger();

        ledger.setBusiness(business);

        ledger.setSupplier(supplier);

        ledger.setType(
                SupplierLedger.LedgerType.PURCHASE_RETURN
        );

        /*
         * Link the cancelled original purchase.
         */
        ledger.setPurchase(purchase);

        /*
         * This is NOT an actual PurchaseReturn entity.
         *
         * Therefore purchaseReturn remains null.
         */
        ledger.setPurchaseReturn(null);

        /*
         * Cancellation reverses the complete
         * purchase payable.
         */
        ledger.setDebit(
                BigDecimal.ZERO
        );

        ledger.setCredit(
                purchase.getGrandTotal() != null
                        ? purchase.getGrandTotal()
                        : BigDecimal.ZERO
        );

        ledger.setBalance(
                BigDecimal.ZERO
        );

        ledger.setDescription(
                "Purchase cancelled "
                        + purchase.getPurchaseNumber()
        );

        /*
         * Keep cancellation entry after
         * the original purchase entry.
         */
        ledger.setCreatedAt(
                LocalDateTime.now()
        );

        SupplierLedger saved =
                supplierLedgerRepository.save(
                        ledger
                );

        /*
         * Recalculate all supplier balances.
         */
        rebuildSupplierBalance(
                business,
                supplier
        );

        return saved;
    }
}