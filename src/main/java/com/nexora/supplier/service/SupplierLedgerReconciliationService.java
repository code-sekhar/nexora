package com.nexora.supplier.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.business.entity.Business;
import com.nexora.purchase.entity.Purchase;
import com.nexora.purchase.repository.PurchaseRepository;
import com.nexora.supplier.entity.Supplier;
import com.nexora.supplier.entity.SupplierLedger;
import com.nexora.supplier.entity.SupplierLedger.LedgerType;
import com.nexora.supplier.repository.SupplierLedgerRepository;
import com.nexora.supplier.repository.SupplierRepository;

@Service
@Transactional
public class SupplierLedgerReconciliationService {

    private final SupplierRepository supplierRepository;
    private final SupplierLedgerRepository supplierLedgerRepository;
    private final PurchaseRepository purchaseRepository;

    public SupplierLedgerReconciliationService(
            SupplierRepository supplierRepository,
            SupplierLedgerRepository supplierLedgerRepository,
            PurchaseRepository purchaseRepository) {

        this.supplierRepository = supplierRepository;
        this.supplierLedgerRepository = supplierLedgerRepository;
        this.purchaseRepository = purchaseRepository;
    }

    /**
     * Reconcile all suppliers of a business.
     */
    public int reconcileBusiness(Business business) {

        List<Supplier> suppliers =
                supplierRepository.findByBusinessId(business.getId());

        int createdEntries = 0;

        for (Supplier supplier : suppliers) {
            createdEntries += reconcileSupplier(business, supplier);
        }

        return createdEntries;
    }

    /**
     * Reconcile one supplier.
     */
    public int reconcileSupplier(
            Business business,
            Supplier supplier) {

        List<Purchase> purchases =
                purchaseRepository
                        .findBySupplierIdAndBusinessIdOrderByCreatedAtAsc(
                                supplier.getId(),
                                business.getId()
                        );

        int createdEntries = 0;

        for (Purchase purchase : purchases) {

            /*
             * RECEIVED PURCHASE
             *
             * Ensure PURCHASE ledger entry exists.
             */
            if (purchase.getStatus() == Purchase.PurchaseStatus.RECEIVED) {

                boolean purchaseExists =
                        supplierLedgerRepository
                                .existsByPurchaseIdAndType(
                                        purchase.getId(),
                                        LedgerType.PURCHASE
                                );

                if (!purchaseExists) {

                    createPurchaseEntry(
                            business,
                            supplier,
                            purchase
                    );

                    createdEntries++;
                }
            }

            /*
             * CANCELLED PURCHASE
             *
             * We still need the original PURCHASE entry
             * for historical accuracy.
             *
             * Then we create PURCHASE_RETURN entry.
             */
            else if (purchase.getStatus() == Purchase.PurchaseStatus.CANCELLED) {

                boolean purchaseExists =
                        supplierLedgerRepository
                                .existsByPurchaseIdAndType(
                                        purchase.getId(),
                                        LedgerType.PURCHASE
                                );

                if (!purchaseExists) {

                    createPurchaseEntry(
                            business,
                            supplier,
                            purchase
                    );

                    createdEntries++;
                }

                boolean purchaseReturnExists =
                        supplierLedgerRepository
                                .existsByPurchaseIdAndType(
                                        purchase.getId(),
                                        LedgerType.PURCHASE_RETURN
                                );

                if (!purchaseReturnExists) {

                    createPurchaseReturnEntry(
                            business,
                            supplier,
                            purchase
                    );

                    createdEntries++;
                }
            }
        }

        /*
         * Rebuild running balances after reconciliation.
         */
        rebuildSupplierBalance(
                business,
                supplier
        );

        return createdEntries;
    }

    /**
     * Create PURCHASE debit entry.
     */
    public SupplierLedger createPurchaseEntry(
            Business business,
            Supplier supplier,
            Purchase purchase) {

        Optional<SupplierLedger> existing =
                supplierLedgerRepository
                        .findByPurchaseIdAndType(
                                purchase.getId(),
                                SupplierLedger.LedgerType.PURCHASE
                        );

        if (existing.isPresent()) {
            return existing.get();
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

        ledger.setCredit(BigDecimal.ZERO);
        ledger.setBalance(BigDecimal.ZERO);

        ledger.setCreatedAt(
                purchase.getCreatedAt() != null
                        ? purchase.getCreatedAt()
                        : java.time.LocalDateTime.now()
        );

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

    /**
     * Create PURCHASE_RETURN credit entry.
     */
    public SupplierLedger createPurchaseReturnEntry(
            Business business,
            Supplier supplier,
            Purchase purchase) {

        Optional<SupplierLedger> existing =
                supplierLedgerRepository
                        .findByPurchaseIdAndType(
                                purchase.getId(),
                                SupplierLedger.LedgerType.PURCHASE_RETURN
                        );

        if (existing.isPresent()) {
            return existing.get();
        }

        SupplierLedger ledger =
                new SupplierLedger();

        ledger.setBusiness(business);
        ledger.setSupplier(supplier);

        ledger.setType(
                SupplierLedger.LedgerType.PURCHASE_RETURN
        );

        ledger.setPurchase(purchase);

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

        ledger.setCreatedAt(
                purchase.getUpdatedAt() != null
                        ? purchase.getUpdatedAt()
                        : java.time.LocalDateTime.now()
        );

        ledger.setDescription(
                "Purchase Return " +
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

    /**
     * Rebuild supplier running balance.
     */
    private void rebuildSupplierBalance(
            Business business,
            Supplier supplier) {

        List<SupplierLedger> entries =
                supplierLedgerRepository
                        .findBySupplierIdAndBusinessIdOrderByCreatedAtAsc(
                                supplier.getId(),
                                business.getId()
                        );

        BigDecimal balance = BigDecimal.ZERO;

        for (SupplierLedger entry : entries) {

            BigDecimal debit =
                    entry.getDebit() != null
                            ? entry.getDebit()
                            : BigDecimal.ZERO;

            BigDecimal credit =
                    entry.getCredit() != null
                            ? entry.getCredit()
                            : BigDecimal.ZERO;

            balance = balance
                    .add(debit)
                    .subtract(credit);

            entry.setBalance(balance);
        }

        supplierLedgerRepository.saveAll(entries);
    }
}