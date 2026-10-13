package com.nexora.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ExpensePaymentResponse {

    private Long id;

    private Long businessId;

    private Long expenseId;
    private String expenseNumber;

    private Long accountId;
    private String accountName;

    private String paymentNumber;

    private LocalDate paymentDate;

    private BigDecimal amount;

    private String referenceNumber;
    private String notes;

    private Long accountTransactionId;
    private String accountTransactionNumber;

    private BigDecimal expensePaidAmount;
    private BigDecimal expenseDueAmount;
    private String expenseStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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

    public Long getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(Long expenseId) {
        this.expenseId = expenseId;
    }

    public String getExpenseNumber() {
        return expenseNumber;
    }

    public void setExpenseNumber(String expenseNumber) {
        this.expenseNumber = expenseNumber;
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

    public String getPaymentNumber() {
        return paymentNumber;
    }

    public void setPaymentNumber(String paymentNumber) {
        this.paymentNumber = paymentNumber;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public void setAccountTransactionNumber(String accountTransactionNumber) {
        this.accountTransactionNumber = accountTransactionNumber;
    }

    public BigDecimal getExpensePaidAmount() {
        return expensePaidAmount;
    }

    public void setExpensePaidAmount(BigDecimal expensePaidAmount) {
        this.expensePaidAmount = expensePaidAmount;
    }

    public BigDecimal getExpenseDueAmount() {
        return expenseDueAmount;
    }

    public void setExpenseDueAmount(BigDecimal expenseDueAmount) {
        this.expenseDueAmount = expenseDueAmount;
    }

    public String getExpenseStatus() {
        return expenseStatus;
    }

    public void setExpenseStatus(String expenseStatus) {
        this.expenseStatus = expenseStatus;
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