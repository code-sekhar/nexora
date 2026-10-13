package com.nexora.tax.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TaxProfileRequest {

    @NotBlank(message = "Tax profile name is required")
    @Size(max = 100, message = "Tax profile name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Tax profile code is required")
    @Size(max = 50, message = "Tax profile code must not exceed 50 characters")
    private String code;

    @NotBlank(message = "Tax type is required")
    @Size(max = 50, message = "Tax type must not exceed 50 characters")
    private String taxType;

    public TaxProfileRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTaxType() {
        return taxType;
    }

    public void setTaxType(String taxType) {
        this.taxType = taxType;
    }
}