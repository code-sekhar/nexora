package com.nexora.purchase.returnmodule.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexora.purchase.returnmodule.entity.PurchaseReturn;

public interface PurchaseReturnRepository
        extends JpaRepository<PurchaseReturn, Long> {

    List<PurchaseReturn>
    findByBusinessIdOrderByCreatedAtDesc(
            Long businessId
    );

    Optional<PurchaseReturn>
    findByIdAndBusinessId(
            Long id,
            Long businessId
    );
}