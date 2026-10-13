package com.nexora.reconciliation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReconciliationItemResponse {

    private Long id;

    private Long businessId;

    private Long reconciliationId;

    private String reconciliationNumber;

    private Long accountTransactionId;

    private String accountTransactionNumber;

    private Long accountId;

    private String accountName;

    private String transactionType;

    private BigDecimal transactionAmount;

    private LocalDateTime transactionDate;

    private String transactionDescription;

    private String statementReference;

    private LocalDateTime statementDate;

    private BigDecimal statementAmount;

    private String statementDescription;

    private String matchStatus;

    private String notes;

    private LocalDateTime createdAt;

    // getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public Long getReconciliationId() {
        return reconciliationId;
    }

    public void setReconciliationId(Long reconciliationId) {
        this.reconciliationId = reconciliationId;
    }

    public String getReconciliationNumber() {
        return reconciliationNumber;
    }

    public void setReconciliationNumber(String reconciliationNumber) {
        this.reconciliationNumber = reconciliationNumber;
    }

    public Long getAccountTransactionId() {
        return accountTransactionId;
    }

    public void setAccountTransactionId(Long accountTransactionId) {
        this.accountTransactionId = accountTransactionId;
    }

    public String getAccountTransactionNumber() {
        return accountTransactionNumber;
    }

    public void setAccountTransactionNumber(
            String accountTransactionNumber) {
        this.accountTransactionNumber = accountTransactionNumber;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(
            BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTransactionDescription() {
        return transactionDescription;
    }

    public void setTransactionDescription(
            String transactionDescription) {
        this.transactionDescription = transactionDescription;
    }

    public String getStatementReference() {
        return statementReference;
    }

    public void setStatementReference(String statementReference) {
        this.statementReference = statementReference;
    }

    public LocalDateTime getStatementDate() {
        return statementDate;
    }

    public void setStatementDate(LocalDateTime statementDate) {
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

    public void setStatementDescription(
            String statementDescription) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}