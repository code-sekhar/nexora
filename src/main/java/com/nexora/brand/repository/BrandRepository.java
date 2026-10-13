package com.nexora.brand.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexora.brand.entity.Brand;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    List<Brand> findByBusinessId(Long businessId);

    Optional<Brand> findByIdAndBusinessId(
            Long id,
            Long businessId
    );

    boolean existsByNameAndBusinessId(
            String name,
            Long businessId
    );

    boolean existsByNameAndBusinessIdAndIdNot(
            String name,
            Long businessId,
            Long id
    );
}