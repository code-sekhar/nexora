package com.nexora.supplier.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexora.supplier.entity.Supplier;

public interface SupplierRepository
        extends JpaRepository<Supplier, Long> {

    List<Supplier> findByBusinessId(Long businessId);

    Optional<Supplier> findByIdAndBusinessId(
            Long id,
            Long businessId
    );

    boolean existsByPhoneAndBusinessId(
            String phone,
            Long businessId
    );

    boolean existsByPhoneAndBusinessIdAndIdNot(
            String phone,
            Long businessId,
            Long id
    );
}