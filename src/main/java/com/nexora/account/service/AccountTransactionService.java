package com.nexora.account.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.account.dto.AccountTransactionRequest;
import com.nexora.account.dto.AccountTransactionResponse;
import com.nexora.account.entity.Account;
import com.nexora.account.entity.AccountTransaction;
import com.nexora.account.entity.AccountTransaction.TransactionType;
import com.nexora.account.exception.AccountTransactionException;
import com.nexora.account.repository.AccountRepository;
import com.nexora.account.repository.AccountTransactionRepository;
import com.nexora.business.entity.Business;

@Service
public class AccountTransactionService {

    private final AccountRepository accountRepository;
    private final AccountTransactionRepository transactionRepository;

    public AccountTransactionService(
            AccountRepository accountRepository,
            AccountTransactionRepository transactionRepository) {

        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public AccountTransactionResponse createTransaction(
            AccountTransactionRequest request,
            Business business) {

        validateRequest(request);

        Account account =
                accountRepository.findAccountForUpdate(
                        request.getAccountId(),
                        business.getId()
                ).orElseThrow(() ->
                        new AccountTransactionException(
                                "Account not found"
                        )
                );

        if (!account.getActive()) {
            throw new AccountTransactionException(
                    "Cannot create transaction for inactive account"
            );
        }

        BigDecimal amount = request.getAmount();

        /*
         * TRANSFER
         */
        if (request.getTransactionType() == TransactionType.TRANSFER) {

            return createTransfer(
                    request,
                    business,
                    account,
                    amount
            );
        }

        BigDecimal balanceBefore =
                account.getCurrentBalance();

        BigDecimal balanceAfter;

        /*
         * CREDIT
         */
        if (request.getTransactionType() == TransactionType.CREDIT) {

            balanceAfter =
                    balanceBefore.add(amount);

        }

        /*
         * DEBIT
         */
        else {

            if (balanceBefore.compareTo(amount) < 0) {

                throw new AccountTransactionException(
                        "Insufficient account balance"
                );
            }

            balanceAfter =
                    balanceBefore.subtract(amount);
        }

        /*
         * Update account balance
         */
        account.setCurrentBalance(balanceAfter);

        accountRepository.save(account);

        /*
         * Create transaction record
         */
        AccountTransaction transaction =
                new AccountTransaction();

        transaction.setBusiness(business);

        transaction.setAccount(account);

        transaction.setTransactionType(
                request.getTransactionType()
        );

        transaction.setAmount(amount);

        transaction.setBalanceBefore(
                balanceBefore
        );

        transaction.setBalanceAfter(
                balanceAfter
        );

        transaction.setTransactionNumber(
                generateTransactionNumber(
                        business.getId()
                )
        );

        transaction.setTransactionDate(
                request.getTransactionDate() != null
                        ? request.getTransactionDate()
                        : LocalDateTime.now()
        );

        transaction.setDescription(
                request.getDescription()
        );

        transaction.setReferenceType(
                request.getReferenceType()
        );

        transaction.setReferenceId(
                request.getReferenceId()
        );

        transaction.setReferenceNumber(
                request.getReferenceNumber()
        );

        transactionRepository.save(transaction);

        return mapToResponse(transaction);
    }

    /*
     * =========================================================
     * TRANSFER
     * =========================================================
     */

    @Transactional
    protected AccountTransactionResponse createTransfer(
            AccountTransactionRequest request,
            Business business,
            Account sourceAccount,
            BigDecimal amount) {

        /*
         * Destination account is required
         */
        if (request.getTransferAccountId() == null) {

            throw new AccountTransactionException(
                    "Transfer account is required"
            );
        }

        /*
         * Source and destination cannot be same
         */
        if (sourceAccount.getId().equals(
                request.getTransferAccountId())) {

            throw new AccountTransactionException(
                    "Source and destination accounts cannot be same"
            );
        }

        /*
         * Lock destination account
         */
        Account destinationAccount =
                accountRepository.findAccountForUpdate(
                        request.getTransferAccountId(),
                        business.getId()
                ).orElseThrow(() ->
                        new AccountTransactionException(
                                "Transfer account not found"
                        )
                );

        /*
         * Destination account must be active
         */
        if (!destinationAccount.getActive()) {

            throw new AccountTransactionException(
                    "Cannot transfer to inactive account"
            );
        }

        /*
         * Check source balance
         */
        BigDecimal sourceBefore =
                sourceAccount.getCurrentBalance();

        if (sourceBefore.compareTo(amount) < 0) {

            throw new AccountTransactionException(
                    "Insufficient account balance"
            );
        }

        /*
         * Calculate source balance
         */
        BigDecimal sourceAfter =
                sourceBefore.subtract(amount);

        /*
         * Calculate destination balance
         */
        BigDecimal destinationBefore =
                destinationAccount.getCurrentBalance();

        BigDecimal destinationAfter =
                destinationBefore.add(amount);

        /*
         * Update source account
         */
        sourceAccount.setCurrentBalance(
                sourceAfter
        );

        /*
         * Update destination account
         */
        destinationAccount.setCurrentBalance(
                destinationAfter
        );

        accountRepository.save(sourceAccount);

        accountRepository.save(destinationAccount);

        /*
         * Create one TRANSFER transaction.
         */
        AccountTransaction transaction =
                new AccountTransaction();

        transaction.setBusiness(business);

        /*
         * Source account
         */
        transaction.setAccount(
                sourceAccount
        );

        /*
         * Destination account
         */
        transaction.setTransferAccount(
                destinationAccount
        );

        transaction.setTransactionType(
                TransactionType.TRANSFER
        );

        transaction.setAmount(
                amount
        );

        /*
         * Balance information belongs
         * to the source account.
         */
        transaction.setBalanceBefore(
                sourceBefore
        );

        transaction.setBalanceAfter(
                sourceAfter
        );

        transaction.setTransactionNumber(
                generateTransactionNumber(
                        business.getId()
                )
        );

        transaction.setTransactionDate(
                request.getTransactionDate() != null
                        ? request.getTransactionDate()
                        : LocalDateTime.now()
        );

        transaction.setDescription(
                request.getDescription() != null
                        ? request.getDescription()
                        : "Account transfer"
        );

        transaction.setReferenceType(
                request.getReferenceType() != null
                        ? request.getReferenceType()
                        : "TRANSFER"
        );

        transaction.setReferenceId(
                request.getReferenceId()
        );

        transaction.setReferenceNumber(
                request.getReferenceNumber()
        );

        transactionRepository.save(transaction);

        return mapToResponse(transaction);
    }

    /*
     * =========================================================
     * GET ALL TRANSACTIONS
     * =========================================================
     */

    public List<AccountTransactionResponse> getAllTransactions(
            Business business) {

        return transactionRepository
                .findByBusinessIdOrderByTransactionDateDesc(
                        business.getId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /*
     * =========================================================
     * GET ACCOUNT TRANSACTIONS
     * =========================================================
     */

    public List<AccountTransactionResponse> getAccountTransactions(
            Long accountId,
            Business business) {

        /*
         * Verify account belongs to current business.
         */
        accountRepository
                .findByIdAndBusinessId(
                        accountId,
                        business.getId()
                )
                .orElseThrow(() ->
                        new AccountTransactionException(
                                "Account not found"
                        )
                );

        return transactionRepository
                .findByBusinessIdAndAccountIdOrderByTransactionDateDesc(
                        business.getId(),
                        accountId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /*
     * =========================================================
     * GET ACCOUNT TRANSACTIONS BY DATE RANGE
     * =========================================================
     */

    public List<AccountTransactionResponse>
    getAccountTransactionsByDateRange(
            Long accountId,
            LocalDateTime start,
            LocalDateTime end,
            Business business) {

        /*
         * Verify account belongs to current business.
         */
        accountRepository
                .findByIdAndBusinessId(
                        accountId,
                        business.getId()
                )
                .orElseThrow(() ->
                        new AccountTransactionException(
                                "Account not found"
                        )
                );

        return transactionRepository
                .findByBusinessIdAndAccountIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                        business.getId(),
                        accountId,
                        start,
                        end
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
            AccountTransactionRequest request) {

        if (request == null) {

            throw new AccountTransactionException(
                    "Transaction request is required"
            );
        }

        if (request.getAccountId() == null) {

            throw new AccountTransactionException(
                    "Account ID is required"
            );
        }

        if (request.getTransactionType() == null) {

            throw new AccountTransactionException(
                    "Transaction type is required"
            );
        }

        if (request.getAmount() == null ||
                request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {

            throw new AccountTransactionException(
                    "Transaction amount must be greater than zero"
            );
        }
    }

    /*
     * =========================================================
     * TRANSACTION NUMBER
     * =========================================================
     */

    private String generateTransactionNumber(
            Long businessId) {

        return "ATX-"
                + businessId
                + "-"
                + System.currentTimeMillis()
                + "-"
                + UUID.randomUUID()
                    .toString()
                    .substring(0, 6)
                    .toUpperCase();
    }

    /*
     * =========================================================
     * RESPONSE MAPPER
     * =========================================================
     */

    private AccountTransactionResponse mapToResponse(
            AccountTransaction transaction) {

        AccountTransactionResponse response =
                new AccountTransactionResponse();

        response.setId(
                transaction.getId()
        );

        response.setBusinessId(
                transaction.getBusiness().getId()
        );

        response.setAccountId(
                transaction.getAccount().getId()
        );

        response.setAccountName(
                transaction.getAccount().getName()
        );

        if (transaction.getTransferAccount() != null) {

            response.setTransferAccountId(
                    transaction.getTransferAccount().getId()
            );

            response.setTransferAccountName(
                    transaction.getTransferAccount().getName()
            );
        }

        response.setTransactionType(
                transaction.getTransactionType()
        );

        response.setAmount(
                transaction.getAmount()
        );

        response.setBalanceBefore(
                transaction.getBalanceBefore()
        );

        response.setBalanceAfter(
                transaction.getBalanceAfter()
        );

        response.setTransactionNumber(
                transaction.getTransactionNumber()
        );

        response.setTransactionDate(
                transaction.getTransactionDate()
        );

        response.setDescription(
                transaction.getDescription()
        );

        response.setReferenceType(
                transaction.getReferenceType()
        );

        response.setReferenceId(
                transaction.getReferenceId()
        );

        response.setReferenceNumber(
                transaction.getReferenceNumber()
        );

        response.setCreatedAt(
                transaction.getCreatedAt()
        );

        return response;
    }
}