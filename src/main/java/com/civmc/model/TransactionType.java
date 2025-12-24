package com.civmc.model;

public enum TransactionType {
    DEPOSIT("Deposit"),
    WITHDRAW("Withdraw"),
    CLAIM("Claim Purchase"),
    UPGRADE("Level Upgrade"),
    PENALTY("Penalty"),
    TAX("Tax Collection"),
    REFUND("Refund"),
    TRANSFER("Transfer");
    
    private final String displayName;
    
    TransactionType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}