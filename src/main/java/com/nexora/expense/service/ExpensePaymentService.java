package com.nexora.expense.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.account.dto.AccountTransactionRequest;
import com.nexora.account.dto.AccountTransactionResponse;
import com.nexora.account.entity.Account;
import com.nexora.account.repository.AccountRepository;
import com.nexora.account.service.AccountTransactionService;
import com.nexora.business.entity.Business;
import com.nexora.expense.dto.ExpensePaymentRequest;
import com.nexora.expense.dto.ExpensePaymentResponse;
import com.nexora.expense.entity.Expense;
import com.nexora.expense.entity.ExpensePayment;
import com.nexora.expense.exception.ExpensePaymentException;
import com.nexora.expense.repository.ExpensePaymentRepository;
import com.nexora.expense.repository.ExpenseRepository;

@Service
@Transactional
public class ExpensePaymentService {

    private final ExpensePaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;
    private final AccountRepository accountRepository;
    private final AccountTransactionService accountTransactionService;

    public ExpensePaymentService(
            ExpensePaymentRepository paymentRepository,
            ExpenseRepository expenseRepository,
            AccountRepository accountRepository,
            AccountTransactionService accountTransactionService) {

        this.paymentRepository = paymentRepository;
        this.expenseRepository = expenseRepository;
        this.accountRepository = accountRepository;
        this.accountTransactionService =
                accountTransactionService;
    }

    /*
     * =========================================================
     * CREATE EXPENSE PAYMENT
     * =========================================================
     */

    public ExpensePaymentResponse createPayment(
            Business business,
            ExpensePaymentRequest request) {

        validateRequest(request);

        /*
         * Lock expense row.
         *
         * This prevents two users from paying the same
         * expense simultaneously and exceeding the due amount.
         */
        Expense expense =
                expenseRepository.findExpenseForUpdate(
                                request.getExpenseId(),
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new ExpensePaymentException(
                                        "Expense not found"
                                )
                        );

        /*
         * Cancelled expense cannot receive payment.
         */
        if (expense.getStatus() ==
                Expense.ExpenseStatus.CANCELLED) {

            throw new ExpensePaymentException(
                    "Cannot make payment for a cancelled expense"
            );
        }

        /*
         * Expense must have outstanding due.
         */
        BigDecimal dueAmount =
                expense.getDueAmount();

        if (dueAmount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new ExpensePaymentException(
                    "Expense has no outstanding due"
            );
        }

        BigDecimal paymentAmount =
                request.getAmount();

        /*
         * Payment cannot exceed due.
         */
        if (paymentAmount.compareTo(dueAmount) > 0) {

            throw new ExpensePaymentException(
                    "Payment amount cannot exceed expense due"
            );
        }

        /*
         * Lock account.
         */
        Account account =
                accountRepository
                        .findAccountForUpdate(
                                request.getAccountId(),
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new ExpensePaymentException(
                                        "Account not found"
                                )
                        );

        /*
         * Account must be active.
         */
        if (!Boolean.TRUE.equals(account.getActive())) {

            throw new ExpensePaymentException(
                    "Cannot make payment from inactive account"
            );
        }

        /*
         * Payment date.
         */
        LocalDate paymentDate =
                request.getPaymentDate() == null ||
                request.getPaymentDate().isBlank()
                        ? LocalDate.now()
                        : LocalDate.parse(
                                request.getPaymentDate()
                        );

        /*
         * =====================================================
         * CREATE PAYMENT RECORD
         * =====================================================
         *
         * Save payment first so that we get payment ID.
         *
         * If account transaction fails later,
         * @Transactional will rollback this payment too.
         */

        ExpensePayment payment =
                new ExpensePayment();

        payment.setBusiness(business);

        payment.setExpense(expense);

        payment.setAccount(account);

        payment.setPaymentNumber(
                generatePaymentNumber(
                        business.getId()
                )
        );

        payment.setPaymentDate(
                paymentDate
        );

        payment.setAmount(
                paymentAmount
        );

        payment.setReferenceNumber(
                request.getReferenceNumber()
        );

        payment.setNotes(
                request.getNotes()
        );

        ExpensePayment savedPayment =
                paymentRepository.save(payment);

        /*
         * =====================================================
         * ACCOUNT TRANSACTION
         * =====================================================
         *
         * Expense payment always means money going OUT.
         *
         * Therefore AccountTransaction = DEBIT.
         */

        AccountTransactionRequest transactionRequest =
                new AccountTransactionRequest();

        transactionRequest.setAccountId(
                account.getId()
        );

        transactionRequest.setTransactionType(
                com.nexora.account.entity.AccountTransaction
                        .TransactionType.DEBIT
        );

        transactionRequest.setAmount(
                paymentAmount
        );

        transactionRequest.setTransactionDate(
                paymentDate.atStartOfDay()
        );

        transactionRequest.setDescription(
                "Expense payment - "
                + expense.getExpenseNumber()
        );

        transactionRequest.setReferenceType(
                "EXPENSE_PAYMENT"
        );

        transactionRequest.setReferenceId(
                savedPayment.getId()
        );

        transactionRequest.setReferenceNumber(
                savedPayment.getPaymentNumber()
        );

        AccountTransactionResponse transactionResponse =
                accountTransactionService.createTransaction(
                        transactionRequest,
                        business
                );

        /*
         * Save account transaction information
         * inside payment for traceability.
         */

        savedPayment.setAccountTransactionId(
                transactionResponse.getId()
        );

        savedPayment.setAccountTransactionNumber(
                transactionResponse.getTransactionNumber()
        );

        /*
         * =====================================================
         * UPDATE EXPENSE
         * =====================================================
         */

        BigDecimal newPaidAmount =
                expense.getPaidAmount()
                        .add(paymentAmount);

        BigDecimal newDueAmount =
                expense.getTotalAmount()
                        .subtract(newPaidAmount);

        expense.setPaidAmount(
                newPaidAmount
        );

        expense.setDueAmount(
                newDueAmount
        );

        /*
         * Determine new status.
         */

        if (newDueAmount.compareTo(
                BigDecimal.ZERO) == 0) {

            expense.setStatus(
                    Expense.ExpenseStatus.PAID
            );

        } else {

            expense.setStatus(
                    Expense.ExpenseStatus.PARTIAL
            );
        }

        expenseRepository.save(expense);

        /*
         * Save payment with transaction information.
         */
        paymentRepository.save(savedPayment);

        return mapToResponse(savedPayment);
    }

    /*
     * =========================================================
     * GET ALL PAYMENTS
     * =========================================================
     */

    @Transactional(readOnly = true)
    public List<ExpensePaymentResponse> getAllPayments(
            Business business) {

        return paymentRepository
                .findByBusinessIdOrderByPaymentDateDescCreatedAtDesc(
                        business.getId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /*
     * =========================================================
     * GET PAYMENT BY ID
     * =========================================================
     */

    @Transactional(readOnly = true)
    public ExpensePaymentResponse getPaymentById(
            Business business,
            Long id) {

        ExpensePayment payment =
                paymentRepository
                        .findByIdAndBusinessId(
                                id,
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new ExpensePaymentException(
                                        "Expense payment not found"
                                )
                        );

        return mapToResponse(payment);
    }

    /*
     * =========================================================
     * GET PAYMENTS BY EXPENSE
     * =========================================================
     */

    @Transactional(readOnly = true)
    public List<ExpensePaymentResponse> getPaymentsByExpense(
            Business business,
            Long expenseId) {

        /*
         * Verify expense belongs to business.
         */
        expenseRepository
                .findByIdAndBusinessId(
                        expenseId,
                        business.getId()
                )
                .orElseThrow(() ->
                        new ExpensePaymentException(
                                "Expense not found"
                        )
                );

        return paymentRepository
                .findByBusinessIdAndExpenseIdOrderByPaymentDateDescCreatedAtDesc(
                        business.getId(),
                        expenseId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /*
     * =========================================================
     * GET PAYMENTS BY DATE RANGE
     * =========================================================
     */

    @Transactional(readOnly = true)
    public List<ExpensePaymentResponse> getPaymentsByDateRange(
            Business business,
            LocalDate startDate,
            LocalDate endDate) {

        if (startDate == null ||
                endDate == null) {

            throw new ExpensePaymentException(
                    "Start date and end date are required"
            );
        }

        if (startDate.isAfter(endDate)) {

            throw new ExpensePaymentException(
                    "Start date cannot be after end date"
            );
        }

        return paymentRepository
                .findByBusinessIdAndPaymentDateBetweenOrderByPaymentDateDesc(
                        business.getId(),
                        startDate,
                        endDate
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /*
     * =========================================================
     * VALIDATION
     * =========================================================
     */

    private void validateRequest(
            ExpensePaymentRequest request) {

        if (request == null) {

            throw new ExpensePaymentException(
                    "Payment request is required"
            );
        }

        if (request.getExpenseId() == null) {

            throw new ExpensePaymentException(
                    "Expense ID is required"
            );
        }

        if (request.getAccountId() == null) {

            throw new ExpensePaymentException(
                    "Account ID is required"
            );
        }

        if (request.getAmount() == null ||
                request.getAmount()
                        .compareTo(BigDecimal.ZERO) <= 0) {

            throw new ExpensePaymentException(
                    "Payment amount must be greater than zero"
            );
        }
    }

    /*
     * =========================================================
     * PAYMENT NUMBER
     * =========================================================
     */

    private String generatePaymentNumber(
            Long businessId) {

        String number;

        do {

            number =
                    "EPM-"
                    + businessId
                    + "-"
                    + System.currentTimeMillis()
                    + "-"
                    + UUID.randomUUID()
                            .toString()
                            .substring(0, 6)
                            .toUpperCase();

        } while (
                paymentRepository
                        .existsByBusinessIdAndPaymentNumber(
                                businessId,
                                number
                        )
        );

        return number;
    }

    /*
     * =========================================================
     * RESPONSE MAPPER
     * =========================================================
     */

    private ExpensePaymentResponse mapToResponse(
            ExpensePayment payment) {

        ExpensePaymentResponse response =
                new ExpensePaymentResponse();

        response.setId(
                payment.getId()
        );

        response.setBusinessId(
                payment.getBusiness().getId()
        );

        response.setExpenseId(
                payment.getExpense().getId()
        );

        response.setExpenseNumber(
                payment.getExpense().getExpenseNumber()
        );

        response.setAccountId(
                payment.getAccount().getId()
        );

        response.setAccountName(
                payment.getAccount().getName()
        );

        response.setPaymentNumber(
                payment.getPaymentNumber()
        );

        response.setPaymentDate(
                payment.getPaymentDate()
        );

        response.setAmount(
                payment.getAmount()
        );

        response.setReferenceNumber(
                payment.getReferenceNumber()
        );

        response.setNotes(
                payment.getNotes()
        );

        response.setAccountTransactionId(
                payment.getAccountTransactionId()
        );

        response.setAccountTransactionNumber(
                payment.getAccountTransactionNumber()
        );

        response.setExpensePaidAmount(
                payment.getExpense().getPaidAmount()
        );

        response.setExpenseDueAmount(
                payment.getExpense().getDueAmount()
        );

        response.setExpenseStatus(
                payment.getExpense()
                        .getStatus()
                        .name()
        );

        response.setCreatedAt(
                payment.getCreatedAt()
        );

        response.setUpdatedAt(
                payment.getUpdatedAt()
        );

        return response;
    }
}