package com.nexora.reconciliation.dto;

import java.math.BigDecimal;

public class ReconciliationRequest {

    private Long accountId;

    private String statementStartDate;

    private String statementEndDate;

    private BigDecimal openingStatementBalance;

    private BigDecimal closingStatementBalance;

    private String notes;

    // getters and setters
    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getStatementStartDate() {
        return statementStartDate;
    }

    public void setStatementStartDate(String statementStartDate) {
        this.statementStartDate = statementStartDate;
    }

    public String getStatementEndDate() {
        return statementEndDate;
    }

    public void setStatementEndDate(String statementEndDate) {
        this.statementEndDate = statementEndDate;
    }

    public BigDecimal getOpeningStatementBalance() {
        return openingStatementBalance;
    }

    public void setOpeningStatementBalance(
            BigDecimal openingStatementBalance) {
        this.openingStatementBalance = openingStatementBalance;
    }

    public BigDecimal getClosingStatementBalance() {
        return closingStatementBalance;
    }

    public void setClosingStatementBalance(
            BigDecimal closingStatementBalance) {
        this.closingStatementBalance = closingStatementBalance;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}