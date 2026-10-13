package com.nexora.expense.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexora.expense.entity.Expense;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository
        extends JpaRepository<Expense, Long> {

    Optional<Expense> findByIdAndBusinessId(
            Long id,
            Long businessId
    );
    @Query(value = """
    	    SELECT *
    	    FROM expenses
    	    WHERE id = :id
    	    AND business_id = :businessId
    	    FOR UPDATE
    	    """, nativeQuery = true)
    	Optional<Expense> findExpenseForUpdate(
    	        @Param("id") Long id,
    	        @Param("businessId") Long businessId
    	);

    List<Expense> findByBusinessIdOrderByExpenseDateDescCreatedAtDesc(
            Long businessId
    );

    List<Expense> findByBusinessIdAndExpenseDateBetweenOrderByExpenseDateDesc(
            Long businessId,
            LocalDate startDate,
            LocalDate endDate
    );

    boolean existsByBusinessIdAndExpenseNumber(
            Long businessId,
            String expenseNumber
    );
   
}