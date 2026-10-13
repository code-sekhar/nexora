package com.nexora.sales.returnmodule.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nexora.sales.returnmodule.entity.SalesReturnItem;

public interface SalesReturnItemRepository
        extends JpaRepository<SalesReturnItem, Long> {

    @Query("""
        SELECT COALESCE(SUM(sri.quantity), 0)
        FROM SalesReturnItem sri
        WHERE sri.invoiceItem.id = :invoiceItemId
        AND sri.salesReturn.status = com.nexora.sales.returnmodule.entity.SalesReturn$ReturnStatus.COMPLETED
    """)
    BigDecimal getReturnedQuantity(
            @Param("invoiceItemId")
            Long invoiceItemId
    );
}