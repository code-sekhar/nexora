package com.nexora.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexora.payment.entity.Payment;

public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    List<Payment> findByBusinessIdOrderByPaymentDateDesc(
            Long businessId
    );

    List<Payment> findByCustomerIdAndBusinessIdOrderByPaymentDateDesc(
            Long customerId,
            Long businessId
    );

    List<Payment> findByInvoiceIdAndBusinessIdOrderByPaymentDateDesc(
            Long invoiceId,
            Long businessId
    );

    Optional<Payment> findByIdAndBusinessId(
            Long id,
            Long businessId
    );
   // boolean existsByPaymentId(Long paymentId);
}