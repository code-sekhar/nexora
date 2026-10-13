package com.nexora.reconciliation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReconciliationResponse {

    private Long id;

    private Long businessId;

    private Long accountId;

    private String accountName;

    private String reconciliationNumber;

    private String statementStartDate;

    private String statementEndDate;

    private BigDecimal openingStatementBalance;

    private BigDecimal closingStatementBalance;

    private BigDecimal systemBalance;

    private BigDecimal differenceAmount;

    private String status;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

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

    public String getReconciliationNumber() {
        return reconciliationNumber;
    }

    public void setReconciliationNumber(String reconciliationNumber) {
        this.reconciliationNumber = reconciliationNumber;
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

    public BigDecimal getSystemBalance() {
        return systemBalance;
    }

    public void setSystemBalance(BigDecimal systemBalance) {
        this.systemBalance = systemBalance;
    }

    public BigDecimal getDifferenceAmount() {
        return differenceAmount;
    }

    public void setDifferenceAmount(BigDecimal differenceAmount) {
        this.differenceAmount = differenceAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}