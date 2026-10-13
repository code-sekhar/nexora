package com.nexora.purchase.returnmodule.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nexora.purchase.returnmodule.entity.PurchaseReturnItem;

public interface PurchaseReturnItemRepository
        extends JpaRepository<PurchaseReturnItem, Long> {

    @Query("""
        SELECT COALESCE(SUM(i.quantity), 0)
        FROM PurchaseReturnItem i
        WHERE i.purchaseItem.id = :purchaseItemId
        AND i.purchaseReturn.status = 'COMPLETED'
    """)
    BigDecimal getReturnedQuantity(
            @Param("purchaseItemId")
            Long purchaseItemId
    );
}