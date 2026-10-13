package com.nexora.customer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexora.customer.entity.Customer;

public interface CustomerRepository
        extends JpaRepository<Customer, Long> {

    List<Customer> findByBusinessId(Long businessId);

    Optional<Customer> findByIdAndBusinessId(
            Long id,
            Long businessId
    );

    boolean existsByPhoneAndBusinessId(
            String phone,
            Long businessId
    );

    boolean existsByPhoneAndBusinessIdAndIdNot(
            String phone,
            Long businessId,
            Long id
    );
}