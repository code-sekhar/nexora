package com.nexora.supplier.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexora.supplier.entity.SupplierLedger;

public interface SupplierLedgerRepository
        extends JpaRepository<SupplierLedger, Long> {

    List<SupplierLedger>
    findBySupplierIdAndBusinessIdOrderByCreatedAtAsc(
            Long supplierId,
            Long businessId
    );

    List<SupplierLedger>
    findByBusinessIdOrderByCreatedAtAsc(
            Long businessId
    );

    boolean existsByPurchaseIdAndType(
            Long purchaseId,
            SupplierLedger.LedgerType type
    );
    
    boolean existsByPurchaseReturnId(
            Long purchaseReturnId
    );
    
    Optional<SupplierLedger> findByPurchaseIdAndType(
            Long purchaseId,
            SupplierLedger.LedgerType type
    );
    Optional<SupplierLedger>
    findByPaymentId(Long paymentId);
}