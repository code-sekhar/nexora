package com.nexora.purchase.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexora.purchase.entity.PurchaseItem;

public interface PurchaseItemRepository
        extends JpaRepository<PurchaseItem, Long> {

    List<PurchaseItem> findByPurchaseId(Long purchaseId);
}