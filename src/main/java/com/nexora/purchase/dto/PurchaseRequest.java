package com.nexora.purchase.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PurchaseRequest {

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    @Size(max = 100)
    private String supplierInvoiceNumber;

    private LocalDate purchaseDate;

    @DecimalMin(
        value = "0.00",
        message = "Paid amount cannot be negative"
    )
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @NotEmpty(message = "At least one purchase item is required")
    @Valid
    private List<PurchaseItemRequest> items;


    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierInvoiceNumber() {
        return supplierInvoiceNumber;
    }

    public void setSupplierInvoiceNumber(
            String supplierInvoiceNumber) {

        this.supplierInvoiceNumber =
            supplierInvoiceNumber;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public List<PurchaseItemRequest> getItems() {
        return items;
    }

    public void setItems(
            List<PurchaseItemRequest> items) {

        this.items = items;
    }
}