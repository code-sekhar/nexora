package com.nexora.invoice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexora.invoice.entity.Invoice;

public interface InvoiceRepository
        extends JpaRepository<Invoice, Long> {

    List<Invoice> findByBusinessIdOrderByCreatedAtDesc(
            Long businessId
    );

    Optional<Invoice> findByIdAndBusinessId(
            Long id,
            Long businessId
    );

    boolean existsByInvoiceNumberAndBusinessId(
            String invoiceNumber,
            Long businessId
    );
    List<Invoice>
    findByCustomerIdAndBusinessIdOrderByCreatedAtAsc(
            Long customerId,
            Long businessId
    );
}