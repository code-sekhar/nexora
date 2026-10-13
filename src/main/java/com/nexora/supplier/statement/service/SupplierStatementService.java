package com.nexora.supplier.statement.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.business.entity.Business;
import com.nexora.supplier.entity.Supplier;
import com.nexora.supplier.entity.SupplierLedger;
import com.nexora.supplier.repository.SupplierLedgerRepository;
import com.nexora.supplier.statement.dto.SupplierStatementEntryResponse;
import com.nexora.supplier.statement.dto.SupplierStatementResponse;

@Service
@Transactional(readOnly = true)
public class SupplierStatementService {

    private final SupplierLedgerRepository supplierLedgerRepository;

    public SupplierStatementService(
            SupplierLedgerRepository supplierLedgerRepository) {

        this.supplierLedgerRepository =
                supplierLedgerRepository;
    }

    public SupplierStatementResponse getStatement(
            Business business,
            Supplier supplier,
            LocalDate fromDate,
            LocalDate toDate) {

        if (fromDate != null
                && toDate != null
                && fromDate.isAfter(toDate)) {

            throw new IllegalArgumentException(
                    "From date cannot be after to date"
            );
        }

        List<SupplierLedger> allEntries =
                supplierLedgerRepository
                        .findBySupplierIdAndBusinessIdOrderByCreatedAtAsc(
                                supplier.getId(),
                                business.getId()
                        );

        /*
         * Calculate the balance immediately before
         * the statement period.
         */
        BigDecimal openingBalance =
                BigDecimal.ZERO;

        if (fromDate != null) {

            LocalDateTime periodStart =
                    fromDate.atStartOfDay();

            for (SupplierLedger ledger : allEntries) {

                if (ledger.getCreatedAt() == null) {
                    continue;
                }

                if (ledger.getCreatedAt()
                        .isBefore(periodStart)) {

                    openingBalance =
                            openingBalance
                                    .add(safe(ledger.getDebit()))
                                    .subtract(
                                            safe(ledger.getCredit())
                                    );
                }
            }
        }

        LocalDateTime periodStart =
                fromDate != null
                        ? fromDate.atStartOfDay()
                        : null;

        LocalDateTime periodEnd =
                toDate != null
                        ? toDate.atTime(
                                LocalTime.MAX
                        )
                        : null;

        List<SupplierStatementEntryResponse> statementEntries =
                new ArrayList<>();

        BigDecimal totalPurchases =
                BigDecimal.ZERO;

        BigDecimal totalPayments =
                BigDecimal.ZERO;

        BigDecimal totalPurchaseReturns =
                BigDecimal.ZERO;

        BigDecimal totalDebits =
                BigDecimal.ZERO;

        BigDecimal totalCredits =
                BigDecimal.ZERO;

        /*
         * Start running balance from opening balance.
         */
        BigDecimal runningBalance =
                openingBalance;

        for (SupplierLedger ledger : allEntries) {

            if (ledger.getCreatedAt() == null) {
                continue;
            }

            LocalDateTime ledgerDate =
                    ledger.getCreatedAt();

            /*
             * Before selected period.
             */
            if (periodStart != null
                    && ledgerDate.isBefore(
                            periodStart
                    )) {

                continue;
            }

            /*
             * After selected period.
             */
            if (periodEnd != null
                    && ledgerDate.isAfter(
                            periodEnd
                    )) {

                continue;
            }

            BigDecimal debit =
                    safe(ledger.getDebit());

            BigDecimal credit =
                    safe(ledger.getCredit());

            runningBalance =
                    runningBalance
                            .add(debit)
                            .subtract(credit);

            totalDebits =
                    totalDebits.add(debit);

            totalCredits =
                    totalCredits.add(credit);

            switch (ledger.getType()) {

                case PURCHASE:

                    totalPurchases =
                            totalPurchases.add(debit);

                    break;

                case PAYMENT:

                    totalPayments =
                            totalPayments.add(credit);

                    break;

                case PURCHASE_RETURN:

                    totalPurchaseReturns =
                            totalPurchaseReturns
                                    .add(credit);

                    break;

                default:
                    break;
            }

            statementEntries.add(
                    mapToStatementEntry(
                            ledger,
                            runningBalance
                    )
            );
        }

        /*
         * For no date filter, closing balance is
         * the current supplier balance.
         *
         * For filtered report, closing balance is
         * balance at the end of selected period.
         */
        BigDecimal closingBalance =
                runningBalance;

        /*
         * Current actual due is always calculated
         * from the complete ledger.
         */
        BigDecimal currentDue =
                calculateCurrentDue(allEntries);

        SupplierStatementResponse response =
                new SupplierStatementResponse();

        response.setSupplierId(
                supplier.getId()
        );

        response.setSupplierName(
                supplier.getName()
        );

        response.setFromDate(
                fromDate
        );

        response.setToDate(
                toDate
        );

        response.setOpeningBalance(
                openingBalance
        );

        response.setTotalPurchases(
                totalPurchases
        );

        response.setTotalPayments(
                totalPayments
        );

        response.setTotalPurchaseReturns(
                totalPurchaseReturns
        );

        response.setTotalDebits(
                totalDebits
        );

        response.setTotalCredits(
                totalCredits
        );

        response.setClosingBalance(
                closingBalance
        );

        response.setCurrentDue(
                currentDue
        );

        response.setTransactionCount(
                statementEntries.size()
        );

        response.setEntries(
                statementEntries
        );

        return response;
    }

    private BigDecimal calculateCurrentDue(
            List<SupplierLedger> entries) {

        BigDecimal balance =
                BigDecimal.ZERO;

        for (SupplierLedger ledger : entries) {

            balance =
                    balance
                            .add(
                                    safe(
                                            ledger.getDebit()
                                    )
                            )
                            .subtract(
                                    safe(
                                            ledger.getCredit()
                                    )
                            );
        }

        return balance;
    }

    private SupplierStatementEntryResponse
            mapToStatementEntry(
                    SupplierLedger ledger,
                    BigDecimal runningBalance) {

        SupplierStatementEntryResponse response =
                new SupplierStatementEntryResponse();

        response.setLedgerId(
                ledger.getId()
        );

        response.setDate(
                ledger.getCreatedAt()
        );

        response.setType(
                ledger.getType().name()
        );

        response.setDebit(
                safe(ledger.getDebit())
        );

        response.setCredit(
                safe(ledger.getCredit())
        );

        response.setBalance(
                runningBalance
        );

        response.setDescription(
                ledger.getDescription()
        );

        /*
         * Purchase reference
         */
        if (ledger.getPurchase() != null) {

            response.setPurchaseId(
                    ledger.getPurchase().getId()
            );

            response.setReference(
                    "PURCHASE-" +
                    ledger.getPurchase().getId()
            );
        }

        /*
         * Payment reference
         */
        if (ledger.getPayment() != null) {

            response.setPaymentId(
                    ledger.getPayment().getId()
            );

            response.setPaymentReference(
                    ledger.getPayment()
                            .getReferenceNumber()
            );

            /*
             * Payment reference is more useful
             * as statement reference.
             */
            if (ledger.getPayment()
                    .getReferenceNumber() != null
                    && !ledger.getPayment()
                            .getReferenceNumber()
                            .isBlank()) {

                response.setReference(
                        ledger.getPayment()
                                .getReferenceNumber()
                );
            }
        }

        return response;
    }

    private BigDecimal safe(BigDecimal value) {

        return value != null
                ? value
                : BigDecimal.ZERO;
    }
}