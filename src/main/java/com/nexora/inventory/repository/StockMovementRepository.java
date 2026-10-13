package com.nexora.inventory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexora.inventory.entity.StockMovement;

public interface StockMovementRepository
        extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByProductIdAndBusinessIdOrderByCreatedAtDesc(
            Long productId,
            Long businessId);
}