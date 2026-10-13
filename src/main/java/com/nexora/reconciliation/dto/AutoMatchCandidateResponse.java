package com.nexora.reconciliation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AutoMatchCandidateResponse {

    private Long accountTransactionId;

    private String accountTransactionNumber;

    private Long accountId;

    private String accountName;

    private String transactionType;

    private BigDecimal transactionAmount;

    private LocalDateTime transactionDate;

    private String transactionDescription;

    private String matchLevel;

    private Integer score;

    // getters and setters

    public Long getAccountTransactionId() {
        return accountTransactionId;
    }

    public void setAccountTransactionId(
            Long accountTransactionId) {
        this.accountTransactionId = accountTransactionId;
    }

    public String getAccountTransactionNumber() {
        return accountTransactionNumber;
    }

    public void setAccountTransactionNumber(
            String accountTransactionNumber) {
        this.accountTransactionNumber =
                accountTransactionNumber;
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

    public void setTransactionDate(
            LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTransactionDescription() {
        return transactionDescription;
    }

    public void setTransactionDescription(
            String transactionDescription) {
        this.transactionDescription =
                transactionDescription;
    }

    public String getMatchLevel() {
        return matchLevel;
    }

    public void setMatchLevel(String matchLevel) {
        this.matchLevel = matchLevel;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}