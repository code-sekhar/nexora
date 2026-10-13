package com.nexora.sales.returnmodule.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexora.sales.returnmodule.entity.SalesReturn;

public interface SalesReturnRepository
        extends JpaRepository<SalesReturn, Long> {

    Optional<SalesReturn> findByIdAndBusinessId(
            Long id,
            Long businessId
    );

    boolean existsByReturnNumber(String returnNumber);
}