package com.nexora.reconciliation.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.nexora.account.entity.AccountTransaction;
import com.nexora.business.entity.Business;

import jakarta.persistence.*;

@Entity
@Table(
    name = "reconciliation_items",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_reconciliation_transaction",
            columnNames = {"reconciliation_id", "account_transaction_id"}
        )
    }
)
public class ReconciliationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reconciliation_id", nullable = false)
    private Reconciliation reconciliation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_transaction_id", nullable = false)
    private AccountTransaction accountTransaction;

    @Column(name = "statement_reference", length = 150)
    private String statementReference;

    @Column(name = "statement_date")
    private LocalDateTime statementDate;

    @Column(
        name = "statement_amount",
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal statementAmount = BigDecimal.ZERO;

    @Column(length = 500)
    private String statementDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MatchStatus matchStatus;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (statementAmount == null) {
            statementAmount = BigDecimal.ZERO;
        }

        if (matchStatus == null) {
            matchStatus = MatchStatus.MATCHED;
        }
    }

    public enum MatchStatus {
        MATCHED,
        PARTIAL,
        UNMATCHED,
        IGNORED
    }

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

    public Reconciliation getReconciliation() {
        return reconciliation;
    }

    public void setReconciliation(Reconciliation reconciliation) {
        this.reconciliation = reconciliation;
    }

    public AccountTransaction getAccountTransaction() {
        return accountTransaction;
    }

    public void setAccountTransaction(
            AccountTransaction accountTransaction) {
        this.accountTransaction = accountTransaction;
    }

    public String getStatementReference() {
        return statementReference;
    }

    public void setStatementReference(String statementReference) {
        this.statementReference = statementReference;
    }

    public LocalDateTime getStatementDate() {
        return statementDate;
    }

    public void setStatementDate(LocalDateTime statementDate) {
        this.statementDate = statementDate;
    }

    public BigDecimal getStatementAmount() {
        return statementAmount;
    }

    public void setStatementAmount(BigDecimal statementAmount) {
        this.statementAmount = statementAmount;
    }

    public String getStatementDescription() {
        return statementDescription;
    }

    public void setStatementDescription(String statementDescription) {
        this.statementDescription = statementDescription;
    }

    public MatchStatus getMatchStatus() {
        return matchStatus;
    }

    public void setMatchStatus(MatchStatus matchStatus) {
        this.matchStatus = matchStatus;
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
}