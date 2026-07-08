package com.vaultflow.orchestrator.dto;

public class AccountDTO {
    private Long id;
    private String accountReference;
    private String accountName;
    private String accountType;
    private Double balance;
    private Double interestRate;

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
