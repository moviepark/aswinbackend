package com.vaultflow.orchestrator.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "tbl_accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String accountReference;

    @NotBlank(message = "Account name is required")
    @Column(nullable = false)
    private String accountName;

    @Column(nullable = false)
    private String accountType;

    private Double balance;

    private Double interestRate;

    public Account() {
    }

    public Account(String accountReference, String accountName, String accountType, Double balance, Double interestRate) {
        this.accountReference = accountReference;
        this.accountName = accountName;
        this.accountType = accountType;
        this.balance = balance;
        this.interestRate = interestRate;
    }

    @PrePersist
    public void onCreate() {
        if (this.accountType == null || this.accountType.isBlank()) {
            this.accountType = "SAVINGS";
        }
        this.accountType = this.accountType.toUpperCase();
        if (this.balance == null) {
            this.balance = 0.0;
        }
    }

    @PreUpdate
    public void onUpdate() {
        if (this.accountType != null) {
            this.accountType = this.accountType.toUpperCase();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAccountReference() { return accountReference; }
    public void setAccountReference(String accountReference) { this.accountReference = accountReference; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }
    public Double getInterestRate() { return interestRate; }
    public void setInterestRate(Double interestRate) { this.interestRate = interestRate; }
}
