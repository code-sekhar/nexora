package com.nexora.account.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.account.dto.AccountRequest;
import com.nexora.account.dto.AccountResponse;
import com.nexora.account.entity.Account;
import com.nexora.account.repository.AccountRepository;
import com.nexora.business.entity.Business;

@Service
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(
            AccountRepository accountRepository) {

        this.accountRepository = accountRepository;
    }

    public AccountResponse create(
            Business business,
            AccountRequest request) {

        validateRequest(request);

        String name =
                request.getName().trim();

        if (accountRepository
                .existsByBusinessIdAndName(
                        business.getId(),
                        name
                )) {

            throw new IllegalArgumentException(
                    "Account already exists: " + name
            );
        }

        BigDecimal openingBalance =
                request.getOpeningBalance() == null
                        ? BigDecimal.ZERO
                        : request.getOpeningBalance();

        if (openingBalance.compareTo(
                BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Opening balance cannot be negative"
            );
        }

        Account account =
                new Account();

        account.setBusiness(business);
        account.setName(name);
        account.setAccountType(
                request.getAccountType()
        );
        account.setBankName(
                request.getBankName()
        );
        account.setAccountNumber(
                request.getAccountNumber()
        );
        account.setHolderName(
                request.getHolderName()
        );

        account.setOpeningBalance(
                openingBalance
        );

        account.setCurrentBalance(
                openingBalance
        );

        account.setCurrencyCode(
                request.getCurrencyCode() == null ||
                request.getCurrencyCode().isBlank()
                        ? business.getCurrencyCode()
                        : request.getCurrencyCode()
        );

        if (request.getActive() != null) {
            account.setActive(
                    request.getActive()
            );
        }

        Account saved =
                accountRepository.save(account);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAll(
            Business business) {

        return accountRepository
                .findByBusinessIdOrderByNameAsc(
                        business.getId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getById(
            Business business,
            Long id) {

        Account account =
                accountRepository
                        .findByIdAndBusinessId(
                                id,
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Account not found"
                                )
                        );

        return mapToResponse(account);
    }

    public AccountResponse update(
            Business business,
            Long id,
            AccountRequest request) {

        validateRequest(request);

        Account account =
                accountRepository
                        .findByIdAndBusinessId(
                                id,
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Account not found"
                                )
                        );

        String name =
                request.getName().trim();

        if (accountRepository
                .existsByBusinessIdAndNameAndIdNot(
                        business.getId(),
                        name,
                        id
                )) {

            throw new IllegalArgumentException(
                    "Account already exists: " + name
            );
        }

        /*
         * Current balance must NOT be overwritten
         * during normal profile update.
         */
        account.setName(name);

        account.setAccountType(
                request.getAccountType()
        );

        account.setBankName(
                request.getBankName()
        );

        account.setAccountNumber(
                request.getAccountNumber()
        );

        account.setHolderName(
                request.getHolderName()
        );

        if (request.getCurrencyCode() != null &&
                !request.getCurrencyCode().isBlank()) {

            account.setCurrencyCode(
                    request.getCurrencyCode()
            );
        }

        if (request.getActive() != null) {
            account.setActive(
                    request.getActive()
            );
        }

        Account updated =
                accountRepository.save(account);

        return mapToResponse(updated);
    }

    public AccountResponse deactivate(
            Business business,
            Long id) {

        Account account =
                accountRepository
                        .findByIdAndBusinessId(
                                id,
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Account not found"
                                )
                        );

        account.setActive(false);

        Account saved =
                accountRepository.save(account);

        return mapToResponse(saved);
    }

    private void validateRequest(
            AccountRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Request is required"
            );
        }

        if (request.getName() == null ||
                request.getName().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Account name is required"
            );
        }

        if (request.getName().trim().length() > 100) {
            throw new IllegalArgumentException(
                    "Account name cannot exceed 100 characters"
            );
        }

        if (request.getAccountType() == null) {
            throw new IllegalArgumentException(
                    "Account type is required"
            );
        }

        if (request.getOpeningBalance() != null &&
                request.getOpeningBalance()
                        .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Opening balance cannot be negative"
            );
        }
    }

    private AccountResponse mapToResponse(
            Account account) {

        AccountResponse response =
                new AccountResponse();

        response.setId(account.getId());

        response.setBusinessId(
                account.getBusiness().getId()
        );

        response.setName(
                account.getName()
        );

        response.setAccountType(
                account.getAccountType().name()
        );

        response.setBankName(
                account.getBankName()
        );

        response.setAccountNumber(
                account.getAccountNumber()
        );

        response.setHolderName(
                account.getHolderName()
        );

        response.setOpeningBalance(
                account.getOpeningBalance()
        );

        response.setCurrentBalance(
                account.getCurrentBalance()
        );

        response.setCurrencyCode(
                account.getCurrencyCode()
        );

        response.setActive(
                account.getActive()
        );

        response.setCreatedAt(
                account.getCreatedAt()
        );

        response.setUpdatedAt(
                account.getUpdatedAt()
        );

        return response;
    }
    public AccountResponse activateAccount(
            Long id,
            Business business) {

        Account account = accountRepository
                .findByIdAndBusinessId(
                        id,
                        business.getId()
                )
                .orElseThrow(() ->
                        new RuntimeException("Account not found")
                );

        if (account.getActive()) {
            throw new RuntimeException(
                    "Account is already active"
            );
        }

        account.setActive(true);

        return mapToResponse(
                accountRepository.save(account)
        );
    }
}