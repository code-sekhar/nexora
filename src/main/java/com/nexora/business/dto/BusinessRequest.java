package com.nexora.business.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BusinessRequest {

    @NotBlank(message = "Business name is required")
    @Size(max = 100, message = "Business name must not exceed 100 characters")
    private String businessName;

    @NotBlank(message = "Business type is required")
    @Size(max = 50, message = "Business type must not exceed 50 characters")
    private String businessType;

    @NotBlank(message = "Phone is required")
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Email(message = "Invalid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;
    
    @Size(max = 2, message = "Country code must not exceed 2 characters")
    private String countryCode;

    @Size(max = 3, message = "Currency code must not exceed 3 characters")
    private String currencyCode;

    @Size(max = 30, message = "Tax system must not exceed 30 characters")
    private String taxSystem;

    @Size(max = 100, message = "Tax registration number must not exceed 100 characters")
    private String taxRegistrationNumber;

    public BusinessRequest() {
    }

    public BusinessRequest(String businessName, String businessType,
                           String phone, String email, String address,
                           String countryCode, String currencyCode,
                           String taxSystem, String taxRegistrationNumber) {
        this.businessName = businessName;
        this.businessType = businessType;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.countryCode = countryCode;
        this.currencyCode = currencyCode;
        this.taxSystem = taxSystem;
        this.taxRegistrationNumber = taxRegistrationNumber;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getTaxSystem() {
        return taxSystem;
    }

    public void setTaxSystem(String taxSystem) {
        this.taxSystem = taxSystem;
    }

    public String getTaxRegistrationNumber() {
        return taxRegistrationNumber;
    }

    public void setTaxRegistrationNumber(String taxRegistrationNumber) {
        this.taxRegistrationNumber = taxRegistrationNumber;
    }
}