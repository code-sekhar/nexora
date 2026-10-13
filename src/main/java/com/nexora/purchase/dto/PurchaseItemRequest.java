package com.nexora.purchase.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class PurchaseItemRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(
        value = "0.001",
        message = "Quantity must be greater than zero"
    )
    private BigDecimal quantity;

    @NotNull(message = "Unit cost is required")
    @DecimalMin(
        value = "0.00",
        message = "Unit cost cannot be negative"
    )
    private BigDecimal unitCost;

    @DecimalMin(
        value = "0.00",
        message = "Discount cannot be negative"
    )
    private BigDecimal discountAmount = BigDecimal.ZERO;


    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
}