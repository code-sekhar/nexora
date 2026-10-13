package com.nexora.customer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CustomerLedgerResponse {

    private Long id;

    private Long customerId;
    private String customerName;

    private Long invoiceId;
    private String invoiceNumber;

    private Long paymentId;
    
    private Long salesReturnId;
    private String salesReturnNumber;

    private String type;

    private BigDecimal debit;
    private BigDecimal credit;
    private BigDecimal balance;

    private String description;

    private LocalDateTime createdAt;

    public CustomerLedgerResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }
    
    public Long getSalesReturnId() {
        return salesReturnId;
    }

    public void setSalesReturnId(Long salesReturnId) {
        this.salesReturnId = salesReturnId;
    }

    public String getSalesReturnNumber() {
        return salesReturnNumber;
    }

    public void setSalesReturnNumber(
            String salesReturnNumber) {

        this.salesReturnNumber = salesReturnNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getDebit() {
        return debit;
    }

    public void setDebit(BigDecimal debit) {
        this.debit = debit;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    public void setCredit(BigDecimal credit) {
        this.credit = credit;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}