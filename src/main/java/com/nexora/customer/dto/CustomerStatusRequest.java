package com.nexora.customer.dto;

import jakarta.validation.constraints.NotNull;

public class CustomerStatusRequest {

    @NotNull
    private Boolean active;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}