package com.nexora.customer.dto;

import java.math.BigDecimal;
import java.util.List;

public class CustomerLedgerSummaryResponse {

    private Long customerId;

    private String customerName;

    private BigDecimal openingBalance;

    private BigDecimal totalSales;

    private BigDecimal totalPayments;

    private BigDecimal totalSalesReturns = BigDecimal.ZERO;

    private BigDecimal currentBalance;

    private List<CustomerLedgerResponse> entries;

    public CustomerLedgerSummaryResponse() {

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

    public BigDecimal getOpeningBalance() {

        return openingBalance;
    }

    public void setOpeningBalance(
            BigDecimal openingBalance) {

        this.openingBalance = openingBalance;
    }

    public BigDecimal getTotalSales() {

        return totalSales;
    }

    public void setTotalSales(
            BigDecimal totalSales) {

        this.totalSales = totalSales;
    }

    public BigDecimal getTotalPayments() {

        return totalPayments;
    }

    public void setTotalPayments(
            BigDecimal totalPayments) {

        this.totalPayments = totalPayments;
    }

    public BigDecimal getTotalSalesReturns() {

        return totalSalesReturns;
    }

    public void setTotalSalesReturns(
            BigDecimal totalSalesReturns) {

        this.totalSalesReturns = totalSalesReturns;
    }

    public BigDecimal getCurrentBalance() {

        return currentBalance;
    }

    public void setCurrentBalance(
            BigDecimal currentBalance) {

        this.currentBalance = currentBalance;
    }

    public List<CustomerLedgerResponse> getEntries() {

        return entries;
    }

    public void setEntries(
            List<CustomerLedgerResponse> entries) {

        this.entries = entries;
    }
}