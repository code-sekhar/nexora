package com.nexora.tax.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TaxRateResponse {

    private Long id;

    private String name;

    private String component;

    private BigDecimal rate;

    private boolean active;

    private Long taxProfileId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public TaxRateResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getTaxProfileId() {
        return taxProfileId;
    }

    public void setTaxProfileId(Long taxProfileId) {
        this.taxProfileId = taxProfileId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}