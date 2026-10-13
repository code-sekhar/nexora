package com.nexora.expense.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexora.expense.entity.ExpenseCategory;

public interface ExpenseCategoryRepository
        extends JpaRepository<ExpenseCategory, Long> {

    List<ExpenseCategory>
    findByBusinessIdOrderByNameAsc(Long businessId);

    Optional<ExpenseCategory>
    findByIdAndBusinessId(Long id, Long businessId);

    boolean existsByBusinessIdAndName(
            Long businessId,
            String name
    );

    boolean existsByBusinessIdAndNameAndIdNot(
            Long businessId,
            String name,
            Long id
    );
}