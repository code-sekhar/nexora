package com.nexora.purchase.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexora.purchase.entity.Purchase;

public interface PurchaseRepository
        extends JpaRepository<Purchase, Long> {

    List<Purchase> findByBusinessIdOrderByCreatedAtDesc(
            Long businessId);

    Optional<Purchase> findByIdAndBusinessId(
            Long id,
            Long businessId);

    boolean existsByPurchaseNumberAndBusinessId(
            String purchaseNumber,
            Long businessId);
    List<Purchase>
    findBySupplierIdAndBusinessIdOrderByCreatedAtAsc(
            Long supplierId,
            Long businessId
    );
    List<Purchase> findByBusinessIdOrderByCreatedAtAsc(
            Long businessId
    );
    Optional<Purchase> findByPurchaseNumberAndBusinessId(
            String purchaseNumber,
            Long businessId
    );
}