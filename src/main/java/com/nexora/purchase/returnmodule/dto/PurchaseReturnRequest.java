package com.nexora.purchase.returnmodule.dto;

import java.time.LocalDate;
import java.util.List;

public class PurchaseReturnRequest {

    private Long purchaseId;

    private LocalDate returnDate;

    private String reason;

    private String notes;

    private List<PurchaseReturnItemRequest> items;

    public Long getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(Long purchaseId) {
        this.purchaseId = purchaseId;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(
            LocalDate returnDate) {
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

    public List<PurchaseReturnItemRequest> getItems() {
        return items;
    }

    public void setItems(
            List<PurchaseReturnItemRequest> items) {
        this.items = items;
    }
}