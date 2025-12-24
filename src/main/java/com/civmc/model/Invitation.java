package com.civmc.model;

public class Invitation {
    private String id;
    private String targetUUID;
    private String civId;
    private String senderUUID;
    private long sentAt;
    private long expiresAt;
    
    public Invitation() {}
    
    public Invitation(String id, String targetUUID, String civId, String senderUUID, long expiresAt) {
        this.id = id;
        this.targetUUID = targetUUID;
        this.civId = civId;
        this.senderUUID = senderUUID;
        this.sentAt = System.currentTimeMillis();
        this.expiresAt = expiresAt;
    }
    
    public Invitation(String targetUUID, String civId, String senderUUID, long expiresAt) {
        this(java.util.UUID.randomUUID().toString(), targetUUID, civId, senderUUID, expiresAt);
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }
    
    public long getTimeLeft() {
        return Math.max(0, expiresAt - System.currentTimeMillis());
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTargetUUID() {
        return targetUUID;
    }
    
    public void setTargetUUID(String targetUUID) {
        this.targetUUID = targetUUID;
    }
    
    public String getCivId() {
        return civId;
    }
    
    public String getCivUUID() {
        return civId;
    }
    
    public void setCivId(String civId) {
        this.civId = civId;
    }
    
    public String getSenderUUID() {
        return senderUUID;
    }
    
    public void setSenderUUID(String senderUUID) {
        this.senderUUID = senderUUID;
    }
    
    public long getSentAt() {
        return sentAt;
    }
    
    public void setSentAt(long sentAt) {
        this.sentAt = sentAt;
    }
    
    public long getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }
}