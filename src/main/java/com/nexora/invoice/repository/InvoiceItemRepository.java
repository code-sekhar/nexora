package com.nexora.invoice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexora.invoice.entity.InvoiceItem;

public interface InvoiceItemRepository
        extends JpaRepository<InvoiceItem, Long> {

    List<InvoiceItem> findByInvoiceId(Long invoiceId);
}