package com.nexora.sales.returnmodule.dto;

import java.time.LocalDate;
import java.util.List;

public class SalesReturnRequest {

    private Long invoiceId;

    private LocalDate returnDate;

    private String reason;

    private String notes;

    private List<SalesReturnItemRequest> items;

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<SalesReturnItemRequest> getItems() {
        return items;
    }

    public void setItems(List<SalesReturnItemRequest> items) {
        this.items = items;
    }
}