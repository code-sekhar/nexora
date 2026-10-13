package com.nexora.reconciliation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexora.reconciliation.entity.Reconciliation;

public interface ReconciliationRepository
        extends JpaRepository<Reconciliation, Long> {

    Optional<Reconciliation> findByIdAndBusinessId(
            Long id,
            Long businessId
    );

    List<Reconciliation> findByBusinessIdOrderByCreatedAtDesc(
            Long businessId
    );

    List<Reconciliation> findByBusinessIdAndAccountIdOrderByCreatedAtDesc(
            Long businessId,
            Long accountId
    );

    List<Reconciliation>
    findByBusinessIdAndStatementEndDateBetweenOrderByStatementEndDateDesc(
            Long businessId,
            LocalDate startDate,
            LocalDate endDate
    );

    boolean existsByBusinessIdAndReconciliationNumber(
            Long businessId,
            String reconciliationNumber
    );
}