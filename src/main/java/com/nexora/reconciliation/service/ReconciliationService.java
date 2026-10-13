package com.nexora.reconciliation.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.account.entity.Account;
import com.nexora.account.repository.AccountRepository;
import com.nexora.business.entity.Business;
import com.nexora.reconciliation.dto.ReconciliationRequest;
import com.nexora.reconciliation.dto.ReconciliationResponse;
import com.nexora.reconciliation.entity.Reconciliation;
import com.nexora.reconciliation.exception.ReconciliationException;
import com.nexora.reconciliation.repository.ReconciliationRepository;

@Service
@Transactional
public class ReconciliationService {

    private final ReconciliationRepository reconciliationRepository;
    private final AccountRepository accountRepository;

    public ReconciliationService(
            ReconciliationRepository reconciliationRepository,
            AccountRepository accountRepository) {

        this.reconciliationRepository = reconciliationRepository;
        this.accountRepository = accountRepository;
    }

    public ReconciliationResponse createReconciliation(
            ReconciliationRequest request,
            Business business) {

        validateRequest(request);

        Account account = accountRepository
                .findByIdAndBusinessId(
                        request.getAccountId(),
                        business.getId()
                )
                .orElseThrow(() ->
                        new ReconciliationException(
                                "Account not found"
                        ));

        LocalDate startDate =
                LocalDate.parse(request.getStatementStartDate());

        LocalDate endDate =
                LocalDate.parse(request.getStatementEndDate());

        if (endDate.isBefore(startDate)) {
            throw new ReconciliationException(
                    "Statement end date cannot be before start date"
            );
        }

        if (!account.getActive()) {
            throw new ReconciliationException(
                    "Cannot reconcile an inactive account"
            );
        }

        BigDecimal opening =
                request.getOpeningStatementBalance();

        BigDecimal closing =
                request.getClosingStatementBalance();

        BigDecimal systemBalance =
                account.getCurrentBalance();

        BigDecimal difference =
                closing.subtract(systemBalance);

        Reconciliation reconciliation =
                new Reconciliation();

        reconciliation.setBusiness(business);
        reconciliation.setAccount(account);

        reconciliation.setReconciliationNumber(
                generateReconciliationNumber(
                        business.getId()
                )
        );

        reconciliation.setStatementStartDate(startDate);
        reconciliation.setStatementEndDate(endDate);

        reconciliation.setOpeningStatementBalance(opening);
        reconciliation.setClosingStatementBalance(closing);

        reconciliation.setSystemBalance(systemBalance);
        reconciliation.setDifferenceAmount(difference);

        if (difference.compareTo(BigDecimal.ZERO) == 0) {
            reconciliation.setStatus(
                    Reconciliation.ReconciliationStatus.RECONCILED
            );
        } else {
            reconciliation.setStatus(
                    Reconciliation.ReconciliationStatus.DISCREPANCY
            );
        }

        reconciliation.setNotes(request.getNotes());

        Reconciliation saved =
                reconciliationRepository.save(reconciliation);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReconciliationResponse> getAll(
            Business business) {

        return reconciliationRepository
                .findByBusinessIdOrderByCreatedAtDesc(
                        business.getId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReconciliationResponse getById(
            Long id,
            Business business) {

        Reconciliation reconciliation =
                reconciliationRepository
                        .findByIdAndBusinessId(
                                id,
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new ReconciliationException(
                                        "Reconciliation not found"
                                ));

        return mapToResponse(reconciliation);
    }

    @Transactional(readOnly = true)
    public List<ReconciliationResponse> getByAccount(
            Long accountId,
            Business business) {

        return reconciliationRepository
                .findByBusinessIdAndAccountIdOrderByCreatedAtDesc(
                        business.getId(),
                        accountId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReconciliationResponse> getByDateRange(
            LocalDate startDate,
            LocalDate endDate,
            Business business) {

        if (endDate.isBefore(startDate)) {
            throw new ReconciliationException(
                    "End date cannot be before start date"
            );
        }

        return reconciliationRepository
                .findByBusinessIdAndStatementEndDateBetweenOrderByStatementEndDateDesc(
                        business.getId(),
                        startDate,
                        endDate
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void validateRequest(
            ReconciliationRequest request) {

        if (request == null) {
            throw new ReconciliationException(
                    "Request cannot be null"
            );
        }

        if (request.getAccountId() == null) {
            throw new ReconciliationException(
                    "Account ID is required"
            );
        }

        if (request.getStatementStartDate() == null) {
            throw new ReconciliationException(
                    "Statement start date is required"
            );
        }

        if (request.getStatementEndDate() == null) {
            throw new ReconciliationException(
                    "Statement end date is required"
            );
        }

        if (request.getOpeningStatementBalance() == null) {
            throw new ReconciliationException(
                    "Opening statement balance is required"
            );
        }

        if (request.getClosingStatementBalance() == null) {
            throw new ReconciliationException(
                    "Closing statement balance is required"
            );
        }

        if (request.getOpeningStatementBalance()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new ReconciliationException(
                    "Opening statement balance cannot be negative"
            );
        }

        if (request.getClosingStatementBalance()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new ReconciliationException(
                    "Closing statement balance cannot be negative"
            );
        }
    }

    private String generateReconciliationNumber(
            Long businessId) {

        String suffix = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 6)
                .toUpperCase();

        return "REC-"
                + businessId
                + "-"
                + System.currentTimeMillis()
                + "-"
                + suffix;
    }

    private ReconciliationResponse mapToResponse(
            Reconciliation entity) {

        ReconciliationResponse response =
                new ReconciliationResponse();

        response.setId(entity.getId());

        response.setBusinessId(
                entity.getBusiness().getId()
        );

        response.setAccountId(
                entity.getAccount().getId()
        );

        response.setAccountName(
                entity.getAccount().getName()
        );

        response.setReconciliationNumber(
                entity.getReconciliationNumber()
        );

        response.setStatementStartDate(
                entity.getStatementStartDate().toString()
        );

        response.setStatementEndDate(
                entity.getStatementEndDate().toString()
        );

        response.setOpeningStatementBalance(
                entity.getOpeningStatementBalance()
        );

        response.setClosingStatementBalance(
                entity.getClosingStatementBalance()
        );

        response.setSystemBalance(
                entity.getSystemBalance()
        );

        response.setDifferenceAmount(
                entity.getDifferenceAmount()
        );

        response.setStatus(
                entity.getStatus().name()
        );

        response.setNotes(entity.getNotes());

        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());

        return response;
    }
}