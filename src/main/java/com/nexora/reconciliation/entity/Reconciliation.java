package com.nexora.reconciliation.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.nexora.account.entity.Account;
import com.nexora.business.entity.Business;

import jakarta.persistence.*;

@Entity
@Table(
    name = "reconciliations",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_reconciliation_business_number",
            columnNames = {"business_id", "reconciliation_number"}
        )
    }
)
public class Reconciliation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "reconciliation_number", nullable = false, length = 100)
    private String reconciliationNumber;

    @Column(name = "statement_start_date", nullable = false)
    private LocalDate statementStartDate;

    @Column(name = "statement_end_date", nullable = false)
    private LocalDate statementEndDate;

    @Column(
        name = "opening_statement_balance",
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal openingStatementBalance = BigDecimal.ZERO;

    @Column(
        name = "closing_statement_balance",
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal closingStatementBalance = BigDecimal.ZERO;

    @Column(
        name = "system_balance",
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal systemBalance = BigDecimal.ZERO;

    @Column(
        name = "difference_amount",
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal differenceAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReconciliationStatus status;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (openingStatementBalance == null) {
            openingStatementBalance = BigDecimal.ZERO;
        }

        if (closingStatementBalance == null) {
            closingStatementBalance = BigDecimal.ZERO;
        }

        if (systemBalance == null) {
            systemBalance = BigDecimal.ZERO;
        }

        if (differenceAmount == null) {
            differenceAmount = BigDecimal.ZERO;
        }

        if (status == null) {
            status = ReconciliationStatus.DRAFT;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ReconciliationStatus {
        DRAFT,
        IN_PROGRESS,
        RECONCILED,
        DISCREPANCY,
        CANCELLED
    }

    // getters and setters
 // getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getReconciliationNumber() {
        return reconciliationNumber;
    }

    public void setReconciliationNumber(String reconciliationNumber) {
        this.reconciliationNumber = reconciliationNumber;
    }

    public LocalDate getStatementStartDate() {
        return statementStartDate;
    }

    public void setStatementStartDate(LocalDate statementStartDate) {
        this.statementStartDate = statementStartDate;
    }

    public LocalDate getStatementEndDate() {
        return statementEndDate;
    }

    public void setStatementEndDate(LocalDate statementEndDate) {
        this.statementEndDate = statementEndDate;
    }

    public BigDecimal getOpeningStatementBalance() {
        return openingStatementBalance;
    }

    public void setOpeningStatementBalance(
            BigDecimal openingStatementBalance) {
        this.openingStatementBalance = openingStatementBalance;
    }

    public BigDecimal getClosingStatementBalance() {
        return closingStatementBalance;
    }

    public void setClosingStatementBalance(
            BigDecimal closingStatementBalance) {
        this.closingStatementBalance = closingStatementBalance;
    }

    public BigDecimal getSystemBalance() {
        return systemBalance;
    }

    public void setSystemBalance(BigDecimal systemBalance) {
        this.systemBalance = systemBalance;
    }

    public BigDecimal getDifferenceAmount() {
        return differenceAmount;
    }

    public void setDifferenceAmount(BigDecimal differenceAmount) {
        this.differenceAmount = differenceAmount;
    }

    public ReconciliationStatus getStatus() {
        return status;
    }

    public void setStatus(ReconciliationStatus status) {
        this.status = status;
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