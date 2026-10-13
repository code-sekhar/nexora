package com.nexora.reconciliation.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.account.entity.AccountTransaction;
import com.nexora.account.repository.AccountTransactionRepository;
import com.nexora.business.entity.Business;
import com.nexora.reconciliation.dto.AutoMatchCandidateResponse;
import com.nexora.reconciliation.dto.AutoMatchRequest;
import com.nexora.reconciliation.entity.Reconciliation;
import com.nexora.reconciliation.entity.ReconciliationItem;
import com.nexora.reconciliation.repository.ReconciliationItemRepository;
import com.nexora.reconciliation.repository.ReconciliationRepository;

@Service
@Transactional(readOnly = true)
public class AutoMatchService {

    private final AccountTransactionRepository accountTransactionRepository;
    private final ReconciliationRepository reconciliationRepository;
    private final ReconciliationItemRepository reconciliationItemRepository;

    public AutoMatchService(
            AccountTransactionRepository accountTransactionRepository,
            ReconciliationRepository reconciliationRepository,
            ReconciliationItemRepository reconciliationItemRepository) {

        this.accountTransactionRepository = accountTransactionRepository;
        this.reconciliationRepository = reconciliationRepository;
        this.reconciliationItemRepository = reconciliationItemRepository;
    }

    public List<AutoMatchCandidateResponse> findMatches(
            AutoMatchRequest request,
            Business business) {

        validateRequest(request);

        Reconciliation reconciliation =
                reconciliationRepository
                        .findByIdAndBusinessId(
                                request.getReconciliationId(),
                                business.getId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Reconciliation not found"));

        if (reconciliation.getStatus()
                == Reconciliation.ReconciliationStatus.CANCELLED) {

            throw new RuntimeException(
                    "Cannot auto-match a cancelled reconciliation");
        }

        LocalDateTime statementDateTime =
                parseStatementDate(request.getStatementDate());

        LocalDate statementDate =
                statementDateTime.toLocalDate();

        LocalDateTime startDate =
                statementDateTime.minusDays(3);

        LocalDateTime endDate =
                statementDateTime.plusDays(3);

        List<AccountTransaction> transactions =
                accountTransactionRepository.findMatchingTransactions(
                        business.getId(),
                        reconciliation.getAccount().getId(),
                        startDate,
                        endDate,
                        request.getStatementAmount());

        return transactions.stream()

                // Already actively matched transactions must not appear.
                .filter(transaction ->
                        !reconciliationItemRepository
                                .existsActiveMatchForTransaction(
                                        business.getId(),
                                        transaction.getId()))

                .map(transaction ->
                        buildCandidate(
                                transaction,
                                request,
                                statementDate))

                .sorted(
                        Comparator
                                .comparing(
                                        AutoMatchCandidateResponse::getScore)
                                .reversed()
                                .thenComparing(
                                        AutoMatchCandidateResponse
                                                ::getTransactionDate,
                                        Comparator.reverseOrder()))

                .collect(Collectors.toList());
    }

    private AutoMatchCandidateResponse buildCandidate(
            AccountTransaction transaction,
            AutoMatchRequest request,
            LocalDate statementDate) {
    	System.out.println("========== AUTO MATCH DEBUG ==========");
    	System.out.println("REQUEST TYPE        = [" + request.getTransactionType() + "]");
    	System.out.println("REQUEST REFERENCE   = [" + request.getStatementReference() + "]");
    	System.out.println("REQUEST DESCRIPTION = [" + request.getStatementDescription() + "]");

    	System.out.println("TRANSACTION TYPE        = [" + transaction.getTransactionType() + "]");
    	System.out.println("TRANSACTION REFERENCE   = [" + transaction.getReferenceNumber() + "]");
    	System.out.println("TRANSACTION DESCRIPTION = [" + transaction.getDescription() + "]");
    	System.out.println("======================================");

        int score = 0;

        // ------------------------------------------------
        // 1. Amount
        // ------------------------------------------------
        if (transaction.getAmount()
                .compareTo(request.getStatementAmount()) == 0) {

            score += 60;
        }

        // ------------------------------------------------
        // 2. Date
        // ------------------------------------------------
        LocalDate transactionDate =
                transaction.getTransactionDate().toLocalDate();

        long dateDifference =
                Math.abs(
                        ChronoUnit.DAYS.between(
                                transactionDate,
                                statementDate));

        if (dateDifference == 0) {

            score += 25;

        } else if (dateDifference == 1) {

            score += 15;

        } else if (dateDifference <= 3) {

            score += 5;
        }

        // ------------------------------------------------
        // 3. Transaction Type
        // ------------------------------------------------
        if (request.getTransactionType() != null
                && !request.getTransactionType().isBlank()
                && transaction.getTransactionType() != null) {

            String requestedType =
                    request.getTransactionType()
                            .trim()
                            .toUpperCase(Locale.ROOT);

            String actualType =
                    transaction.getTransactionType()
                            .name()
                            .toUpperCase(Locale.ROOT);

            if (requestedType.equals(actualType)) {

                score += 20;
            }
        }

        // ------------------------------------------------
        // 4. Reference similarity
        // ------------------------------------------------
        if (isReferenceMatch(
                request.getStatementReference(),
                transaction.getReferenceNumber())) {

            score += 15;
        }

        // ------------------------------------------------
        // 5. Description similarity
        // ------------------------------------------------
        if (isDescriptionMatch(
                request.getStatementDescription(),
                transaction.getDescription())) {

            score += 10;
        }

        // ------------------------------------------------
        // Match Level
        // ------------------------------------------------
        String matchLevel;

        if (score >= 100) {

            matchLevel = "EXACT";

        } else if (score >= 70) {

            matchLevel = "POSSIBLE";

        } else {

            matchLevel = "WEAK";
        }

        AutoMatchCandidateResponse response =
                new AutoMatchCandidateResponse();

        response.setAccountTransactionId(
                transaction.getId());

        response.setAccountTransactionNumber(
                transaction.getTransactionNumber());

        response.setAccountId(
                transaction.getAccount().getId());

        response.setAccountName(
                transaction.getAccount().getName());

        response.setTransactionType(
                transaction.getTransactionType().name());

        response.setTransactionAmount(
                transaction.getAmount());

        response.setTransactionDate(
                transaction.getTransactionDate());

        response.setTransactionDescription(
                transaction.getDescription());

        response.setMatchLevel(matchLevel);

        response.setScore(score);

        return response;
    }

    private boolean isReferenceMatch(
            String statementReference,
            String transactionReference) {

        if (statementReference == null
                || statementReference.isBlank()
                || transactionReference == null
                || transactionReference.isBlank()) {

            return false;
        }

        String statementRef =
                normalize(statementReference);

        String transactionRef =
                normalize(transactionReference);

        return transactionRef.contains(statementRef)
                || statementRef.contains(transactionRef);
    }

    private boolean isDescriptionMatch(
            String statementDescription,
            String transactionDescription) {

        if (statementDescription == null
                || statementDescription.isBlank()
                || transactionDescription == null
                || transactionDescription.isBlank()) {

            return false;
        }

        String statementText =
                normalize(statementDescription);

        String transactionText =
                normalize(transactionDescription);

        // Direct full-text match
        if (statementText.contains(transactionText)
                || transactionText.contains(statementText)) {

            return true;
        }

        // Word-based similarity
        String[] words =
                statementText.split("\\s+");

        int matchedWords = 0;

        for (String word : words) {

            if (word.length() < 3) {
                continue;
            }

            if (transactionText.contains(word)) {
                matchedWords++;
            }
        }

        return matchedWords >= 2;
    }

    private String normalize(String value) {

        return value
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }

    private LocalDateTime parseStatementDate(
            String value) {

        if (value == null || value.isBlank()) {

            throw new RuntimeException(
                    "Statement date is required");
        }

        try {

            return LocalDateTime.parse(value);

        } catch (Exception ignored) {

            try {

                return LocalDate
                        .parse(value)
                        .atStartOfDay();

            } catch (Exception ignoredAgain) {

                throw new RuntimeException(
                        "Invalid statement date. Use yyyy-MM-dd or yyyy-MM-ddTHH:mm:ss");
            }
        }
    }

    private void validateRequest(
            AutoMatchRequest request) {

        if (request == null) {

            throw new RuntimeException(
                    "Auto-match request is required");
        }

        if (request.getReconciliationId() == null) {

            throw new RuntimeException(
                    "Reconciliation ID is required");
        }

        if (request.getStatementAmount() == null
                || request.getStatementAmount()
                        .compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "Statement amount must be greater than zero");
        }

        if (request.getStatementDate() == null
                || request.getStatementDate().isBlank()) {

            throw new RuntimeException(
                    "Statement date is required");
        }
    }
}