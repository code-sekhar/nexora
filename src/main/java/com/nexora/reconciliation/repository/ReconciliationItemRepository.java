package com.nexora.reconciliation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nexora.reconciliation.entity.ReconciliationItem;

public interface ReconciliationItemRepository
        extends JpaRepository<ReconciliationItem, Long> {

    List<ReconciliationItem>
    findByBusinessIdAndReconciliationIdOrderByStatementDateAsc(
            Long businessId,
            Long reconciliationId
    );

    boolean existsByBusinessIdAndReconciliationIdAndAccountTransactionId(
            Long businessId,
            Long reconciliationId,
            Long accountTransactionId
    );

    @Query("""
        SELECT CASE WHEN COUNT(ri) > 0 THEN true ELSE false END
        FROM ReconciliationItem ri
        WHERE ri.business.id = :businessId
        AND ri.accountTransaction.id = :accountTransactionId
        AND ri.matchStatus <> com.nexora.reconciliation.entity.ReconciliationItem$MatchStatus.IGNORED
        """)
    boolean existsActiveMatchForTransaction(
            @Param("businessId") Long businessId,
            @Param("accountTransactionId") Long accountTransactionId
    );
    Optional<ReconciliationItem> findByIdAndBusinessId(
            Long id,
            Long businessId
    );
}