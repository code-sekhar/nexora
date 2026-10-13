package com.nexora.purchase.returnmodule.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.nexora.business.entity.Business;
import com.nexora.purchase.entity.Purchase;
import com.nexora.supplier.entity.Supplier;

import jakarta.persistence.*;

@Entity
@Table(
    name = "purchase_returns",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {
                "business_id",
                "return_number"
            }
        )
    }
)
public class PurchaseReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        name = "return_number",
        nullable = false,
        length = 50
    )
    private String returnNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "purchase_id",
        nullable = false
    )
    private Purchase purchase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "supplier_id",
        nullable = false
    )
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "business_id",
        nullable = false
    )
    private Business business;

    @Column(nullable = false)
    private LocalDate returnDate;

    @Column(
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal subtotal = BigDecimal.ZERO;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReturnStatus status;

    @Column(length = 500)
    private String reason;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(
        mappedBy = "purchaseReturn",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<PurchaseReturnItem> items =
            new ArrayList<>();

    public PurchaseReturn() {
    }

    @PrePersist
    protected void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (returnDate == null) {
            returnDate = LocalDate.now();
        }

        if (status == null) {
            status = ReturnStatus.COMPLETED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ReturnStatus {
        COMPLETED,
        CANCELLED
    }

    public Long getId() {
        return id;
    }

    public String getReturnNumber() {
        return returnNumber;
    }

    public void setReturnNumber(String returnNumber) {
        this.returnNumber = returnNumber;
    }

    public Purchase getPurchase() {
        return purchase;
    }

    public void setPurchase(Purchase purchase) {
        this.purchase = purchase;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
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

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public ReturnStatus getStatus() {
        return status;
    }

    public void setStatus(ReturnStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<PurchaseReturnItem> getItems() {
        return items;
    }

    public void setItems(
            List<PurchaseReturnItem> items) {
        this.items = items;
    }
}