package com.nexora.reconciliation.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.account.entity.AccountTransaction;
import com.nexora.account.repository.AccountTransactionRepository;
import com.nexora.business.entity.Business;
import com.nexora.reconciliation.dto.ReconciliationItemRequest;
import com.nexora.reconciliation.dto.ReconciliationItemResponse;
import com.nexora.reconciliation.entity.Reconciliation;
import com.nexora.reconciliation.entity.ReconciliationItem;
import com.nexora.reconciliation.entity.ReconciliationItem.MatchStatus;
import com.nexora.reconciliation.exception.ReconciliationException;
import com.nexora.reconciliation.repository.ReconciliationItemRepository;
import com.nexora.reconciliation.repository.ReconciliationRepository;

@Service
@Transactional
public class ReconciliationItemService {

    private final ReconciliationItemRepository itemRepository;
    private final ReconciliationRepository reconciliationRepository;
    private final AccountTransactionRepository accountTransactionRepository;

    public ReconciliationItemService(
            ReconciliationItemRepository itemRepository,
            ReconciliationRepository reconciliationRepository,
            AccountTransactionRepository accountTransactionRepository) {

        this.itemRepository = itemRepository;
        this.reconciliationRepository = reconciliationRepository;
        this.accountTransactionRepository =
                accountTransactionRepository;
    }

    public ReconciliationItemResponse createItem(
            Long reconciliationId,
            ReconciliationItemRequest request,
            Business business) {

        validateRequest(request);

        Reconciliation reconciliation =
                reconciliationRepository
                        .findByIdAndBusinessId(
                                reconciliationId,
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new ReconciliationException(
                                        "Reconciliation not found"
                                ));

        if (reconciliation.getStatus()
                == Reconciliation.ReconciliationStatus.CANCELLED) {

            throw new ReconciliationException(
                    "Cannot add item to a cancelled reconciliation"
            );
        }

        AccountTransaction transaction =
                accountTransactionRepository
                        .findByIdAndBusinessId(
                                request.getAccountTransactionId(),
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new ReconciliationException(
                                        "Account transaction not found"
                                ));

        if (!transaction.getAccount().getId()
                .equals(reconciliation.getAccount().getId())) {

            throw new ReconciliationException(
                    "Account transaction does not belong to this reconciliation account"
            );
        }

        boolean alreadyInThisReconciliation =
                itemRepository
                        .existsByBusinessIdAndReconciliationIdAndAccountTransactionId(
                                business.getId(),
                                reconciliationId,
                                transaction.getId()
                        );

        if (alreadyInThisReconciliation) {

            throw new ReconciliationException(
                    "Account transaction is already added to this reconciliation"
            );
        }

        boolean alreadyMatched =
                itemRepository
                        .existsActiveMatchForTransaction(
                                business.getId(),
                                transaction.getId()
                        );

        if (alreadyMatched) {

            throw new ReconciliationException(
                    "Account transaction is already used in another reconciliation"
            );
        }

        BigDecimal statementAmount =
                request.getStatementAmount();

        BigDecimal transactionAmount =
                transaction.getAmount();

        MatchStatus matchStatus =
                parseMatchStatus(request.getMatchStatus());

        if (matchStatus == MatchStatus.MATCHED
                && statementAmount.compareTo(transactionAmount) != 0) {

            throw new ReconciliationException(
                    "Statement amount must match transaction amount for MATCHED status"
            );
        }

        LocalDateTime statementDate =
                parseDateTime(request.getStatementDate());

        ReconciliationItem item =
                new ReconciliationItem();

        item.setBusiness(business);
        item.setReconciliation(reconciliation);
        item.setAccountTransaction(transaction);

        item.setStatementReference(
                request.getStatementReference()
        );

        item.setStatementDate(statementDate);

        item.setStatementAmount(statementAmount);

        item.setStatementDescription(
                request.getStatementDescription()
        );

        item.setMatchStatus(matchStatus);

        item.setNotes(request.getNotes());

        ReconciliationItem saved =
                itemRepository.save(item);

        updateReconciliationStatus(reconciliation);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReconciliationItemResponse> getByReconciliation(
            Long reconciliationId,
            Business business) {

        reconciliationRepository
                .findByIdAndBusinessId(
                        reconciliationId,
                        business.getId()
                )
                .orElseThrow(() ->
                        new ReconciliationException(
                                "Reconciliation not found"
                        ));

        return itemRepository
                .findByBusinessIdAndReconciliationIdOrderByStatementDateAsc(
                        business.getId(),
                        reconciliationId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReconciliationItemResponse getById(
            Long id,
            Business business) {

        ReconciliationItem item =
                itemRepository.findById(id)
                        .orElseThrow(() ->
                                new ReconciliationException(
                                        "Reconciliation item not found"
                                ));

        if (!item.getBusiness().getId()
                .equals(business.getId())) {

            throw new ReconciliationException(
                    "Reconciliation item not found"
            );
        }

        return mapToResponse(item);
    }

    private void updateReconciliationStatus(
            Reconciliation reconciliation) {

        List<ReconciliationItem> items =
                itemRepository
                        .findByBusinessIdAndReconciliationIdOrderByStatementDateAsc(
                                reconciliation.getBusiness().getId(),
                                reconciliation.getId()
                        );

        if (items.isEmpty()) {

            reconciliation.setStatus(
                    Reconciliation.ReconciliationStatus.IN_PROGRESS
            );

            reconciliationRepository.save(reconciliation);

            return;
        }

        boolean hasUnmatched = items.stream()
                .anyMatch(item ->
                        item.getMatchStatus()
                                == MatchStatus.UNMATCHED);

        boolean hasPartial = items.stream()
                .anyMatch(item ->
                        item.getMatchStatus()
                                == MatchStatus.PARTIAL);

        boolean allMatched = items.stream()
                .allMatch(item ->
                        item.getMatchStatus()
                                == MatchStatus.MATCHED
                        || item.getMatchStatus()
                                == MatchStatus.IGNORED);

        if (hasUnmatched || hasPartial) {

            reconciliation.setStatus(
                    Reconciliation.ReconciliationStatus.IN_PROGRESS
            );

        } else if (allMatched) {

            reconciliation.setStatus(
                    Reconciliation.ReconciliationStatus.RECONCILED
            );
        }
    }

    private void validateRequest(
            ReconciliationItemRequest request) {

        if (request == null) {
            throw new ReconciliationException(
                    "Request cannot be null"
            );
        }

        if (request.getAccountTransactionId() == null) {
            throw new ReconciliationException(
                    "Account transaction ID is required"
            );
        }

        if (request.getStatementAmount() == null) {
            throw new ReconciliationException(
                    "Statement amount is required"
            );
        }

        if (request.getStatementAmount()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new ReconciliationException(
                    "Statement amount cannot be negative"
            );
        }

        if (request.getMatchStatus() == null
                || request.getMatchStatus().isBlank()) {

            throw new ReconciliationException(
                    "Match status is required"
            );
        }
    }

    private MatchStatus parseMatchStatus(
            String value) {

        try {

            return MatchStatus.valueOf(
                    value.trim().toUpperCase()
            );

        } catch (IllegalArgumentException ex) {

            throw new ReconciliationException(
                    "Invalid match status. Allowed values: MATCHED, PARTIAL, UNMATCHED, IGNORED"
            );
        }
    }

    private LocalDateTime parseDateTime(
            String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {

            return LocalDateTime.parse(value);

        } catch (Exception ex) {

            try {

                return LocalDate
                        .parse(value)
                        .atStartOfDay();

            } catch (Exception ignored) {

                throw new ReconciliationException(
                        "Invalid statement date"
                );
            }
        }
    }

    private ReconciliationItemResponse mapToResponse(
            ReconciliationItem item) {

        ReconciliationItemResponse response =
                new ReconciliationItemResponse();

        AccountTransaction transaction =
                item.getAccountTransaction();

        response.setId(item.getId());

        response.setBusinessId(
                item.getBusiness().getId()
        );

        response.setReconciliationId(
                item.getReconciliation().getId()
        );

        response.setReconciliationNumber(
                item.getReconciliation()
                        .getReconciliationNumber()
        );

        response.setAccountTransactionId(
                transaction.getId()
        );

        response.setAccountTransactionNumber(
                transaction.getTransactionNumber()
        );

        response.setAccountId(
                transaction.getAccount().getId()
        );

        response.setAccountName(
                transaction.getAccount().getName()
        );

        response.setTransactionType(
                transaction.getTransactionType().name()
        );

        response.setTransactionAmount(
                transaction.getAmount()
        );

        response.setTransactionDate(
                transaction.getTransactionDate()
        );

        response.setTransactionDescription(
                transaction.getDescription()
        );

        response.setStatementReference(
                item.getStatementReference()
        );

        response.setStatementDate(
                item.getStatementDate()
        );

        response.setStatementAmount(
                item.getStatementAmount()
        );

        response.setStatementDescription(
                item.getStatementDescription()
        );

        response.setMatchStatus(
                item.getMatchStatus().name()
        );

        response.setNotes(item.getNotes());

        response.setCreatedAt(item.getCreatedAt());

        return response;
    }
    @Transactional
    public void deleteItem(Long itemId, Business business) {

        ReconciliationItem item =
                itemRepository.findByIdAndBusinessId(
                        itemId,
                        business.getId()
                )
                .orElseThrow(() ->
                        new ReconciliationException(
                                "Reconciliation item not found"
                        )
                );

        Reconciliation reconciliation = item.getReconciliation();

        if (reconciliation.getStatus()
                == Reconciliation.ReconciliationStatus.CANCELLED) {

            throw new ReconciliationException(
                    "Cannot unmatch an item from a cancelled reconciliation"
            );
        }

        itemRepository.delete(item);

        /*
         * Recalculate reconciliation status
         * after removing this item.
         */
        updateReconciliationStatus(reconciliation);
    }
}