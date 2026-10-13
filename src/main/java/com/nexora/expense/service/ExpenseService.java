package com.nexora.expense.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.business.entity.Business;
import com.nexora.expense.dto.ExpenseRequest;
import com.nexora.expense.dto.ExpenseResponse;
import com.nexora.expense.entity.Expense;
import com.nexora.expense.entity.ExpenseCategory;
import com.nexora.expense.repository.ExpenseCategoryRepository;
import com.nexora.expense.repository.ExpenseRepository;

@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryRepository categoryRepository;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            ExpenseCategoryRepository categoryRepository) {

        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
    }

    public ExpenseResponse create(
            Business business,
            ExpenseRequest request) {

        validateRequest(request);

        ExpenseCategory category =
                categoryRepository
                        .findByIdAndBusinessId(
                                request.getCategoryId(),
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Expense category not found"
                                )
                        );

        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new IllegalArgumentException(
                    "Expense category is inactive"
            );
        }

        LocalDate expenseDate =
                request.getExpenseDate() == null ||
                request.getExpenseDate().isBlank()
                        ? LocalDate.now()
                        : LocalDate.parse(
                                request.getExpenseDate()
                        );

        BigDecimal amount =
                request.getAmount();

        BigDecimal taxAmount =
                request.getTaxAmount() == null
                        ? BigDecimal.ZERO
                        : request.getTaxAmount();

        BigDecimal paidAmount =
                request.getPaidAmount() == null
                        ? BigDecimal.ZERO
                        : request.getPaidAmount();

        BigDecimal totalAmount =
                amount.add(taxAmount);

        if (paidAmount.compareTo(totalAmount) > 0) {
            throw new IllegalArgumentException(
                    "Paid amount cannot exceed total expense"
            );
        }

        Expense expense =
                new Expense();

        expense.setBusiness(business);
        expense.setCategory(category);

        expense.setExpenseNumber(
                generateExpenseNumber(
                        business.getId()
                )
        );

        expense.setExpenseDate(expenseDate);
        expense.setDescription(
                request.getDescription()
        );

        expense.setAmount(amount);
        expense.setTaxAmount(taxAmount);
        expense.setTotalAmount(totalAmount);
        expense.setPaidAmount(paidAmount);
        expense.setDueAmount(
                totalAmount.subtract(paidAmount)
        );

        expense.setPaymentMethod(
                request.getPaymentMethod()
        );

        expense.setReferenceNumber(
                request.getReferenceNumber()
        );

        expense.setNotes(
                request.getNotes()
        );

        expense.setStatus(
                calculateStatus(
                        totalAmount,
                        paidAmount
                )
        );

        Expense saved =
                expenseRepository.save(expense);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getAll(
            Business business) {

        return expenseRepository
                .findByBusinessIdOrderByExpenseDateDescCreatedAtDesc(
                        business.getId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getById(
            Business business,
            Long id) {

        Expense expense =
                expenseRepository
                        .findByIdAndBusinessId(
                                id,
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Expense not found"
                                )
                        );

        return mapToResponse(expense);
    }

    public ExpenseResponse update(
            Business business,
            Long id,
            ExpenseRequest request) {

        validateRequest(request);

        Expense expense =
                expenseRepository
                        .findByIdAndBusinessId(
                                id,
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Expense not found"
                                )
                        );

        if (expense.getStatus() ==
                Expense.ExpenseStatus.CANCELLED) {

            throw new IllegalArgumentException(
                    "Cannot update a cancelled expense"
            );
        }

        ExpenseCategory category =
                categoryRepository
                        .findByIdAndBusinessId(
                                request.getCategoryId(),
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Expense category not found"
                                )
                        );

        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new IllegalArgumentException(
                    "Expense category is inactive"
            );
        }

        LocalDate expenseDate =
                request.getExpenseDate() == null ||
                request.getExpenseDate().isBlank()
                        ? expense.getExpenseDate()
                        : LocalDate.parse(
                                request.getExpenseDate()
                        );

        BigDecimal amount =
                request.getAmount();

        BigDecimal taxAmount =
                request.getTaxAmount() == null
                        ? BigDecimal.ZERO
                        : request.getTaxAmount();

        BigDecimal paidAmount =
                request.getPaidAmount() == null
                        ? BigDecimal.ZERO
                        : request.getPaidAmount();

        BigDecimal totalAmount =
                amount.add(taxAmount);

        if (paidAmount.compareTo(totalAmount) > 0) {
            throw new IllegalArgumentException(
                    "Paid amount cannot exceed total expense"
            );
        }

        expense.setCategory(category);
        expense.setExpenseDate(expenseDate);
        expense.setDescription(
                request.getDescription()
        );
        expense.setAmount(amount);
        expense.setTaxAmount(taxAmount);
        expense.setTotalAmount(totalAmount);
        expense.setPaidAmount(paidAmount);
        expense.setDueAmount(
                totalAmount.subtract(paidAmount)
        );
        expense.setPaymentMethod(
                request.getPaymentMethod()
        );
        expense.setReferenceNumber(
                request.getReferenceNumber()
        );
        expense.setNotes(
                request.getNotes()
        );
        expense.setStatus(
                calculateStatus(
                        totalAmount,
                        paidAmount
                )
        );

        Expense updated =
                expenseRepository.save(expense);

        return mapToResponse(updated);
    }

    public ExpenseResponse cancel(
            Business business,
            Long id) {

        Expense expense =
                expenseRepository
                        .findByIdAndBusinessId(
                                id,
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Expense not found"
                                )
                        );

        if (expense.getStatus() ==
                Expense.ExpenseStatus.CANCELLED) {

            throw new IllegalArgumentException(
                    "Expense is already cancelled"
            );
        }

        expense.setStatus(
                Expense.ExpenseStatus.CANCELLED
        );

        Expense saved =
                expenseRepository.save(expense);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getByDateRange(
            Business business,
            LocalDate startDate,
            LocalDate endDate) {

        if (startDate == null ||
                endDate == null) {

            throw new IllegalArgumentException(
                    "Start date and end date are required"
            );
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "Start date cannot be after end date"
            );
        }

        return expenseRepository
                .findByBusinessIdAndExpenseDateBetweenOrderByExpenseDateDesc(
                        business.getId(),
                        startDate,
                        endDate
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void validateRequest(
            ExpenseRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Request is required"
            );
        }

        if (request.getCategoryId() == null) {
            throw new IllegalArgumentException(
                    "Expense category is required"
            );
        }

        if (request.getAmount() == null ||
                request.getAmount()
                        .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Expense amount must be greater than zero"
            );
        }

        if (request.getTaxAmount() != null &&
                request.getTaxAmount()
                        .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Tax amount cannot be negative"
            );
        }

        if (request.getPaidAmount() != null &&
                request.getPaidAmount()
                        .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Paid amount cannot be negative"
            );
        }

        if (request.getPaymentMethod() == null) {
            throw new IllegalArgumentException(
                    "Payment method is required"
            );
        }
    }

    private Expense.ExpenseStatus calculateStatus(
            BigDecimal totalAmount,
            BigDecimal paidAmount) {

        if (paidAmount.compareTo(BigDecimal.ZERO) == 0) {
            return Expense.ExpenseStatus.DUE;
        }

        if (paidAmount.compareTo(totalAmount) == 0) {
            return Expense.ExpenseStatus.PAID;
        }

        return Expense.ExpenseStatus.PARTIAL;
    }

    private String generateExpenseNumber(
            Long businessId) {

        String number;

        do {
            number =
                    "EXP-"
                    + businessId
                    + "-"
                    + System.currentTimeMillis()
                    + "-"
                    + UUID.randomUUID()
                            .toString()
                            .substring(0, 6)
                            .toUpperCase();

        } while (
                expenseRepository
                        .existsByBusinessIdAndExpenseNumber(
                                businessId,
                                number
                        )
        );

        return number;
    }

    private ExpenseResponse mapToResponse(
            Expense expense) {

        ExpenseResponse response =
                new ExpenseResponse();

        response.setId(expense.getId());

        response.setBusinessId(
                expense.getBusiness().getId()
        );

        response.setCategoryId(
                expense.getCategory().getId()
        );

        response.setCategoryName(
                expense.getCategory().getName()
        );

        response.setExpenseNumber(
                expense.getExpenseNumber()
        );

        response.setExpenseDate(
                expense.getExpenseDate()
        );

        response.setDescription(
                expense.getDescription()
        );

        response.setAmount(
                expense.getAmount()
        );

        response.setTaxAmount(
                expense.getTaxAmount()
        );

        response.setTotalAmount(
                expense.getTotalAmount()
        );

        response.setPaidAmount(
                expense.getPaidAmount()
        );

        response.setDueAmount(
                expense.getDueAmount()
        );

        response.setPaymentMethod(
                expense.getPaymentMethod() == null
                        ? null
                        : expense.getPaymentMethod().name()
        );

        response.setReferenceNumber(
                expense.getReferenceNumber()
        );

        response.setStatus(
                expense.getStatus().name()
        );

        response.setNotes(
                expense.getNotes()
        );

        response.setCreatedAt(
                expense.getCreatedAt()
        );

        response.setUpdatedAt(
                expense.getUpdatedAt()
        );

        return response;
    }
}