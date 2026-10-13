package com.nexora.supplier.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nexora.supplier.entity.SupplierPayment;

public interface SupplierPaymentRepository
        extends JpaRepository<SupplierPayment, Long> {

    List<SupplierPayment>
    findByBusinessIdOrderByPaymentDateDesc(
            Long businessId
    );

    List<SupplierPayment>
    findBySupplierIdAndBusinessIdOrderByPaymentDateAsc(
            Long supplierId,
            Long businessId
    );

    List<SupplierPayment>
    findByPurchaseIdAndBusinessIdOrderByPaymentDateAsc(
            Long purchaseId,
            Long businessId
    );

    @Query("""
            SELECT COALESCE(SUM(sp.amount), 0)
            FROM SupplierPayment sp
            WHERE sp.purchase.id = :purchaseId
            AND sp.business.id = :businessId
            """)
    BigDecimal findTotalAmountByPurchaseIdAndBusinessId(
            @Param("purchaseId") Long purchaseId,
            @Param("businessId") Long businessId
    );
}