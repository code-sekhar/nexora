package com.nexora.tax.dto;

import jakarta.validation.constraints.NotNull;

public class TaxRateStatusRequest {

    @NotNull
    private Boolean active;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}