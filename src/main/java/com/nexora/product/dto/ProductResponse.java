package com.nexora.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductResponse {

    private Long id;
    private String name;
    private String sku;
    private String barcode;
    private String description;

    private Long categoryId;
    private String categoryName;

    private Long brandId;
    private String brandName;

    private BigDecimal purchasePrice;
    private BigDecimal sellingPrice;

    private String hsnSacCode;

    private Long taxProfileId;
    private String taxProfileName;
    private String taxType;

    private String unit;
    private Integer minimumStock;
    private boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProductResponse() {
    }

    public ProductResponse(
            Long id,
            String name,
            String sku,
            String barcode,
            String description,
            Long categoryId,
            String categoryName,
            Long brandId,
            String brandName,
            BigDecimal purchasePrice,
            BigDecimal sellingPrice,
            String hsnSacCode,
            Long taxProfileId,
            String taxProfileName,
            String taxType,
            String unit,
            Integer minimumStock,
            boolean active,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.id = id;
        this.name = name;
        this.sku = sku;
        this.barcode = barcode;
        this.description = description;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.brandId = brandId;
        this.brandName = brandName;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.hsnSacCode = hsnSacCode;
        this.taxProfileId = taxProfileId;
        this.taxProfileName = taxProfileName;
        this.taxType = taxType;
        this.unit = unit;
        this.minimumStock = minimumStock;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public String getHsnSacCode() {
        return hsnSacCode;
    }

    public void setHsnSacCode(String hsnSacCode) {
        this.hsnSacCode = hsnSacCode;
    }

    public Long getTaxProfileId() {
        return taxProfileId;
    }

    public void setTaxProfileId(Long taxProfileId) {
        this.taxProfileId = taxProfileId;
    }

    public String getTaxProfileName() {
        return taxProfileName;
    }

    public void setTaxProfileName(String taxProfileName) {
        this.taxProfileName = taxProfileName;
    }

    public String getTaxType() {
        return taxType;
    }

    public void setTaxType(String taxType) {
        this.taxType = taxType;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Integer getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(Integer minimumStock) {
        this.minimumStock = minimumStock;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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