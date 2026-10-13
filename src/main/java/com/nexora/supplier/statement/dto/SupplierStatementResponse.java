package com.nexora.supplier.statement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class SupplierStatementResponse {

    private Long supplierId;

    private String supplierName;

    private LocalDate fromDate;

    private LocalDate toDate;

    private BigDecimal openingBalance = BigDecimal.ZERO;

    private BigDecimal totalPurchases = BigDecimal.ZERO;

    private BigDecimal totalPayments = BigDecimal.ZERO;

    private BigDecimal totalPurchaseReturns = BigDecimal.ZERO;

    private BigDecimal totalDebits = BigDecimal.ZERO;

    private BigDecimal totalCredits = BigDecimal.ZERO;

    private BigDecimal closingBalance = BigDecimal.ZERO;

    private BigDecimal currentDue = BigDecimal.ZERO;

    private Integer transactionCount = 0;

    private List<SupplierStatementEntryResponse> entries;

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(BigDecimal openingBalance) {
        this.openingBalance = openingBalance;
    }

    public BigDecimal getTotalPurchases() {
        return totalPurchases;
    }

    public void setTotalPurchases(BigDecimal totalPurchases) {
        this.totalPurchases = totalPurchases;
    }

    public BigDecimal getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(BigDecimal totalPayments) {
        this.totalPayments = totalPayments;
    }

    public BigDecimal getTotalPurchaseReturns() {
        return totalPurchaseReturns;
    }

    public void setTotalPurchaseReturns(
            BigDecimal totalPurchaseReturns) {
        this.totalPurchaseReturns = totalPurchaseReturns;
    }

    public BigDecimal getTotalDebits() {
        return totalDebits;
    }

    public void setTotalDebits(BigDecimal totalDebits) {
        this.totalDebits = totalDebits;
    }

    public BigDecimal getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(BigDecimal totalCredits) {
        this.totalCredits = totalCredits;
    }

    public BigDecimal getClosingBalance() {
        return closingBalance;
    }

    public void setClosingBalance(BigDecimal closingBalance) {
        this.closingBalance = closingBalance;
    }

    public BigDecimal getCurrentDue() {
        return currentDue;
    }

    public void setCurrentDue(BigDecimal currentDue) {
        this.currentDue = currentDue;
    }

    public Integer getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(Integer transactionCount) {
        this.transactionCount = transactionCount;
    }

    public List<SupplierStatementEntryResponse> getEntries() {
        return entries;
    }

    public void setEntries(
            List<SupplierStatementEntryResponse> entries) {
        this.entries = entries;
    }
}