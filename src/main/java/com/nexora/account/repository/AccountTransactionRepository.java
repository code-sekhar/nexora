package com.nexora.account.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexora.account.entity.AccountTransaction;
import java.math.BigDecimal;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountTransactionRepository
        extends JpaRepository<AccountTransaction, Long> {

    List<AccountTransaction>
    findByBusinessIdAndAccountIdOrderByTransactionDateDesc(
            Long businessId,
            Long accountId
    );

    List<AccountTransaction>
    findByBusinessIdAndAccountIdAndTransactionDateBetweenOrderByTransactionDateDesc(
            Long businessId,
            Long accountId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<AccountTransaction>
    findByBusinessIdOrderByTransactionDateDesc(
            Long businessId
    );
    Optional<AccountTransaction> findByIdAndBusinessId(
            Long id,
            Long businessId
    );
    @Query("""
    	    SELECT t
    	    FROM AccountTransaction t
    	    WHERE t.business.id = :businessId
    	    AND t.account.id = :accountId
    	    AND t.transactionDate BETWEEN :startDate AND :endDate
    	    AND t.amount = :amount
    	    ORDER BY t.transactionDate DESC
    	    """)
    	List<AccountTransaction> findMatchingTransactions(
    	        @Param("businessId") Long businessId,
    	        @Param("accountId") Long accountId,
    	        @Param("startDate") LocalDateTime startDate,
    	        @Param("endDate") LocalDateTime endDate,
    	        @Param("amount") BigDecimal amount
    	);
}