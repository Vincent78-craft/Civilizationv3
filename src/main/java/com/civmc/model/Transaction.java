package com.civmc.model;

public class Transaction {
    private String id;
    private long timestamp;
    private String civId;
    private String actorUUID;
    private TransactionType type;
    private double amount;
    private double balanceAfter;
    private String note;
    
    public Transaction() {}
    
    public Transaction(String id, long timestamp, String civId, String actorUUID, 
                      TransactionType type, double amount, double balanceAfter, String note) {
        this.id = id;
        this.timestamp = timestamp;
        this.civId = civId;
        this.actorUUID = actorUUID;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.note = note;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getCivId() {
        return civId;
    }
    
    public void setCivId(String civId) {
        this.civId = civId;
    }
    
    public String getActorUUID() {
        return actorUUID;
    }
    
    public void setActorUUID(String actorUUID) {
        this.actorUUID = actorUUID;
    }
    
    public TransactionType getType() {
        return type;
    }
    
    public void setType(TransactionType type) {
        this.type = type;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public double getBalanceAfter() {
        return balanceAfter;
    }
    
    public void setBalanceAfter(double balanceAfter) {
        this.balanceAfter = balanceAfter;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
}