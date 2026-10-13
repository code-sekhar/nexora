package com.nexora.product.dto;

public class ProductStatusRequest {

    private boolean active;

    public ProductStatusRequest() {
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}