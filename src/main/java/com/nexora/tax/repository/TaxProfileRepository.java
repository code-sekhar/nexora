package com.nexora.tax.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexora.tax.entity.TaxProfile;

public interface TaxProfileRepository extends JpaRepository<TaxProfile, Long> {

    List<TaxProfile> findByBusinessId(Long businessId);

    Optional<TaxProfile> findByIdAndBusinessId(
            Long id,
            Long businessId
    );

    boolean existsByCodeAndBusinessId(
            String code,
            Long businessId
    );

    boolean existsByCodeAndBusinessIdAndIdNot(
            String code,
            Long businessId,
            Long id
    );
}