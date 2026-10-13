package com.nexora.inventory.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexora.inventory.entity.Inventory;
import com.nexora.invoice.entity.Invoice;

public interface InventoryRepository
        extends JpaRepository<Inventory, Long> {

    List<Inventory> findByBusinessId(Long businessId);

    Optional<Inventory> findByProductIdAndBusinessId(
            Long productId,
            Long businessId);

    boolean existsByProductIdAndBusinessId(
            Long productId,
            Long businessId);
//    List<Invoice> findByCustomerIdAndBusinessIdOrderByCreatedAtAsc(
//            Long customerId,
//            Long businessId
//    );
}