package com.nexora.inventory.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class StockAdjustmentRequest {

    @NotNull(message = "Quantity is required")
    @DecimalMin(
        value = "0.001",
        message = "Quantity must be greater than zero"
    )
    private BigDecimal quantity;

    @NotNull(message = "Adjustment type is required")
    private AdjustmentType type;

    @Size(
        max = 255,
        message = "Reason must not exceed 255 characters"
    )
    private String reason;

    public StockAdjustmentRequest() {
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public AdjustmentType getType() {
        return type;
    }

    public void setType(AdjustmentType type) {
        this.type = type;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public enum AdjustmentType {
        OPENING,
        ADJUSTMENT_IN,
        ADJUSTMENT_OUT,
        DAMAGE
    }
}