package com.nexora.purchase.returnmodule.dto;

import java.math.BigDecimal;

public class PurchaseReturnItemRequest {

    private Long purchaseItemId;

    private BigDecimal quantity;

    public Long getPurchaseItemId() {
        return purchaseItemId;
    }

    public void setPurchaseItemId(
            Long purchaseItemId) {
        this.purchaseItemId = purchaseItemId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(
            BigDecimal quantity) {
        this.quantity = quantity;
    }
}