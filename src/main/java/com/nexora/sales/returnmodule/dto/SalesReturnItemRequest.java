package com.nexora.sales.returnmodule.dto;

import java.math.BigDecimal;

public class SalesReturnItemRequest {

    private Long invoiceItemId;

    private BigDecimal quantity;

    public Long getInvoiceItemId() {
        return invoiceItemId;
    }

    public void setInvoiceItemId(Long invoiceItemId) {
        this.invoiceItemId = invoiceItemId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
}