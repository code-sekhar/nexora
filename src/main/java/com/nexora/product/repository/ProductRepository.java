package com.nexora.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexora.product.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByBusinessId(Long businessId);

    Optional<Product> findByIdAndBusinessId(
            Long id,
            Long businessId
    );

    boolean existsBySkuAndBusinessId(
            String sku,
            Long businessId
    );

    boolean existsBySkuAndBusinessIdAndIdNot(
            String sku,
            Long businessId,
            Long id
    );

    boolean existsByBarcodeAndBusinessId(
            String barcode,
            Long businessId
    );

    boolean existsByBarcodeAndBusinessIdAndIdNot(
            String barcode,
            Long businessId,
            Long id
    );
}