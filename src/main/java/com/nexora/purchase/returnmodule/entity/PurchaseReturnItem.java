package com.nexora.purchase.returnmodule.entity;

import java.math.BigDecimal;

import com.nexora.product.entity.Product;
import com.nexora.purchase.entity.PurchaseItem;

import jakarta.persistence.*;

@Entity
@Table(name = "purchase_return_items")
public class PurchaseReturnItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "purchase_return_id",
        nullable = false
    )
    private PurchaseReturn purchaseReturn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "purchase_item_id",
        nullable = false
    )
    private PurchaseItem purchaseItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "product_id",
        nullable = false
    )
    private Product product;

    @Column(
        nullable = false,
        precision = 15,
        scale = 3
    )
    private BigDecimal quantity;

    @Column(
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal unitCost;

    @Column(
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal taxableAmount = BigDecimal.ZERO;

    @Column(
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal totalAmount = BigDecimal.ZERO;

    public Long getId() {
        return id;
    }

    public PurchaseReturn getPurchaseReturn() {
        return purchaseReturn;
    }

    public void setPurchaseReturn(
            PurchaseReturn purchaseReturn) {
        this.purchaseReturn = purchaseReturn;
    }

    public PurchaseItem getPurchaseItem() {
        return purchaseItem;
    }

    public void setPurchaseItem(
            PurchaseItem purchaseItem) {
        this.purchaseItem = purchaseItem;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
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

    public BigDecimal getTaxableAmount() {
        return taxableAmount;
    }

    public void setTaxableAmount(
            BigDecimal taxableAmount) {
        this.taxableAmount = taxableAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(
            BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}