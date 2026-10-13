package com.nexora.tax.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexora.tax.entity.TaxRate;

public interface TaxRateRepository extends JpaRepository<TaxRate, Long> {

    List<TaxRate> findByTaxProfileId(Long taxProfileId);

    Optional<TaxRate> findByIdAndTaxProfileId(
            Long id,
            Long taxProfileId
    );
}