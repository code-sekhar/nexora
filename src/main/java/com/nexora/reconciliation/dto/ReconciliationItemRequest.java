package com.nexora.reconciliation.dto;

import java.math.BigDecimal;

public class ReconciliationItemRequest {

    private Long accountTransactionId;

    private String statementReference;

    private String statementDate;

    private BigDecimal statementAmount;

    private String statementDescription;

    private String matchStatus;

    private String notes;

    // getters and setters
    public Long getAccountTransactionId() {
        return accountTransactionId;
    }

    public void setAccountTransactionId(
            Long accountTransactionId) {
        this.accountTransactionId = accountTransactionId;
    }

    public String getStatementReference() {
        return statementReference;
    }

    public void setStatementReference(String statementReference) {
        this.statementReference = statementReference;
    }

    public String getStatementDate() {
        return statementDate;
    }

    public void setStatementDate(String statementDate) {
        this.statementDate = statementDate;
    }

    public BigDecimal getStatementAmount() {
        return statementAmount;
    }

    public void setStatementAmount(BigDecimal statementAmount) {
        this.statementAmount = statementAmount;
    }

    public String getStatementDescription() {
        return statementDescription;
    }

    public void setStatementDescription(String statementDescription) {
        this.statementDescription = statementDescription;
    }

    public String getMatchStatus() {
        return matchStatus;
    }

    public void setMatchStatus(String matchStatus) {
        this.matchStatus = matchStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
    
}