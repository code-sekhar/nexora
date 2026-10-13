package com.nexora.customer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexora.customer.entity.CustomerLedger;

public interface CustomerLedgerRepository
        extends JpaRepository<CustomerLedger, Long> {

    List<CustomerLedger>findByCustomerIdAndBusinessIdOrderByCreatedAtAsc(
            Long customerId,
            Long businessId
    );

    List<CustomerLedger>
    findByBusinessIdOrderByCreatedAtAsc(
            Long businessId
    );

    boolean existsByInvoiceIdAndType(
            Long invoiceId,
            CustomerLedger.LedgerType type
    );

    boolean existsByPayment_Id(Long paymentId);
    boolean existsBySalesReturnId(Long salesReturnId);
   
}