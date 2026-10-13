package com.nexora.expense.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexora.expense.entity.ExpensePayment;

public interface ExpensePaymentRepository
        extends JpaRepository<ExpensePayment, Long> {

    Optional<ExpensePayment> findByIdAndBusinessId(
            Long id,
            Long businessId
    );

    List<ExpensePayment>
    findByBusinessIdAndExpenseIdOrderByPaymentDateDescCreatedAtDesc(
            Long businessId,
            Long expenseId
    );

    List<ExpensePayment>
    findByBusinessIdOrderByPaymentDateDescCreatedAtDesc(
            Long businessId
    );

    List<ExpensePayment>
    findByBusinessIdAndPaymentDateBetweenOrderByPaymentDateDesc(
            Long businessId,
            LocalDate startDate,
            LocalDate endDate
    );

    boolean existsByBusinessIdAndPaymentNumber(
            Long businessId,
            String paymentNumber
    );
}