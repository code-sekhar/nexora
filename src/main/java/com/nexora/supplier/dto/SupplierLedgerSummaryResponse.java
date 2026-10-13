package com.nexora.supplier.dto;

import java.math.BigDecimal;
import java.util.List;

public class SupplierLedgerSummaryResponse {

    private Long supplierId;
    private String supplierName;

    private BigDecimal openingBalance;
    private BigDecimal totalPurchases;
    private BigDecimal totalPayments;
    private BigDecimal totalPurchaseReturns;
    private BigDecimal currentDue;

    private List<SupplierLedgerResponse> entries;

    public SupplierLedgerSummaryResponse() {
    }

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

    public void setTotalPurchaseReturns(BigDecimal totalPurchaseReturns) {
        this.totalPurchaseReturns = totalPurchaseReturns;
    }

    public BigDecimal getCurrentDue() {
        return currentDue;
    }

    public void setCurrentDue(BigDecimal currentDue) {
        this.currentDue = currentDue;
    }

    public List<SupplierLedgerResponse> getEntries() {
        return entries;
    }

    public void setEntries(List<SupplierLedgerResponse> entries) {
        this.entries = entries;
    }
}